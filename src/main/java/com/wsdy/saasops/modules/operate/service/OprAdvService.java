package com.wsdy.saasops.modules.operate.service;

import com.wsdy.saasops.common.constants.AdvConstant;
import com.wsdy.saasops.common.constants.Constants;
import com.wsdy.saasops.common.exception.R200Exception;
import com.wsdy.saasops.common.exception.RRException;
import com.wsdy.saasops.common.utils.*;
import com.wsdy.saasops.modules.base.service.BaseService;
import com.wsdy.saasops.modules.member.service.MbrAccountLogService;
import com.wsdy.saasops.modules.operate.dao.*;
import com.wsdy.saasops.modules.operate.entity.*;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@Transactional
public class OprAdvService extends BaseService<OprAdvMapper, OprAdv> {

    @Autowired
    private OprAdvMapper oprAdvMapper;

    @Autowired
    private OprAdvImageMapper oprAdvImageMapper;

    @Value("${opr.adv.excel.path}")
    private String advExcelPath;

    @Autowired
    private QiNiuYunUtil qiNiuYunUtil;

    @Autowired
    private MbrAccountLogService mbrAccountLogService;
    @Autowired
    private OprHelpCategoryMapper oprHelpCategoryMapper;
    @Autowired
    private OprHelpTitleMapper oprHelpTitleMapper;
    @Autowired
    private OprHelpContentMapper oprHelpContentMapper;

    @Transactional
    public void save(OprAdv oprAdv, String username, String ip) {
        OprAdv queryParam = new OprAdv();
        queryParam.setAdvType(oprAdv.getAdvType());
        queryParam.setAdvTypeChild(oprAdv.getAdvTypeChild());
        List<Integer> availables = new ArrayList<>();
        availables.add(1);
        queryParam.setAvailables(availables);
        List<OprAdv> queryOprAdvList = oprAdvMapper.queryOprAdvList(queryParam);
        if (queryOprAdvList != null && queryOprAdvList.size() >= Constants.EVNumber.one) {
            if((1 == queryParam.getAdvType() || 2==queryParam.getAdvType())){
                throw new R200Exception("同一类型不能超过1条数据！");
            }
        }

        oprAdv.setCreater(username);
        oprAdv.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        if (StringUtils.isEmpty(oprAdv.getAdvType()) || oprAdv.getAdvType() != Constants.EVNumber.zero) {//非轮播清空子类型
            oprAdv.setAdvTypeChild(Constants.EVNumber.zero);
        }
        oprAdvMapper.insert(oprAdv);

        //添加操作日志
        mbrAccountLogService.addOprAdvLog(oprAdv, username, ip);

        List<OprAdvImage> imageList = oprAdv.getImageList();
        if(!Collections3.isEmpty(imageList)) {
            for (int i = 0; i < imageList.size(); i++) {
                OprAdvImage oprAdvImage = imageList.get(i);
                oprAdvImage.setAdvId(oprAdv.getId());
                picTarget(oprAdvImage);
                String outStation = oprAdvImage.getOutStation();
                if (null != outStation && !(outStation.contains("http://") || outStation.contains("https://"))) {
                    outStation = "http://" + outStation;
                }
                oprAdvImageMapper.insert(oprAdvImage);
            }
        }
    }

    @Transactional
    public void update(OprAdv oprAdv, String username, String ip) {
        oprAdv.setUpdater(username);
        oprAdv.setUpdateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        if (StringUtils.isEmpty(oprAdv.getAdvType()) || oprAdv.getAdvType() != Constants.EVNumber.zero) {//非轮播清空子类型
            oprAdv.setAdvTypeChild(Constants.EVNumber.zero);
        }
        oprAdvMapper.updateByPrimaryKeySelective(oprAdv);

        //添加广告修改日志
        mbrAccountLogService.editOprAdvLog(oprAdv, username, ip);
        oprAdvMapper.deleteImageById(oprAdv.getId());

        List<OprAdvImage> imageList = oprAdv.getImageList();
        if(!Collections3.isEmpty(imageList)) {
            for (int i = 0; i < imageList.size(); i++) {
                OprAdvImage oprAdvImage = imageList.get(i);
                if (oprAdvImage.getPicTarget().equals(AdvConstant.TARGET_IN)) {
                    oprAdvImage.setOutStation(null);
                } else if (oprAdvImage.getPicTarget().equals(AdvConstant.TARGET_OUT)) {
                    oprAdvImage.setActId(null);
                    oprAdvImage.setActivityId(null);
                }
                String outStation = oprAdvImage.getOutStation();
                if (null != outStation && !(outStation.contains("http://") || outStation.contains("https://"))) {
                    outStation = "http://" + outStation;
                }
                picTarget(oprAdvImage);
                oprAdvImageMapper.insert(oprAdvImage);
            }
        }
    }

    public void picTarget(OprAdvImage oprAdvImage) {
        if (oprAdvImage.getPicTarget().equals(AdvConstant.TARGET_IN)) {
           /* if (null == oprAdvImage.getActId()) {
                throw new R200Exception("活动分类不可为空");
            }*/
        }
        if (oprAdvImage.getPicTarget().equals(AdvConstant.TARGET_OUT)) {
            if (null == oprAdvImage.getOutStation()) {
                throw new R200Exception("站外路径不可为空");
            }
        }
    }

    public void deleteBatch(OprAdv oprAdv, String userName, String ip) {
        Map<String, Object> map = new HashMap<>(2);
        if (null != oprAdv.getIds()) {
            map.put("ids", oprAdv.getIds());
        }
        List<OprAdv> resultList = oprAdvMapper.selectByIds(map);
        oprAdvMapper.deleteByIds(map);
        oprAdvMapper.deleteImageByIds(map);
        for (OprAdv oldOprAdv:resultList) {
            mbrAccountLogService.deleteOprAdvLog(oldOprAdv, userName, ip);
        }
    }

    public PageUtils queryListPage(OprAdv oprAdv, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<OprAdv> list = oprAdvMapper.queryOprAdvList(oprAdv);
        PageUtils page = BeanUtil.toPagedResult(list);
        return page;
    }

    public String getSingleImageUrl(MultipartFile uploadFile) {
        String fileName = null;
        if (Objects.nonNull(uploadFile)) {
            try {
                String prefix = uploadFile.getOriginalFilename()
                        .substring(uploadFile.getOriginalFilename().indexOf("."));
                byte[] fileBuff = IOUtils.toByteArray(uploadFile.getInputStream());
                fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
            } catch (Exception e) {
                log.error("getSingleImageUrljias==error==" + e);
                throw new RRException(e.getMessage());
            }
        }
        return fileName;
    }

    public HashMap getMultiImageUrl(MultipartFile[] uploadFiles) {
        HashMap<String,String> result = new HashMap<>();
        if (Objects.nonNull(uploadFiles)) {
            for(MultipartFile uploadFile : uploadFiles) {
                try {
                    String prefix = uploadFile.getOriginalFilename()
                            .substring(uploadFile.getOriginalFilename().indexOf("."));
//                    String fileNameWithoutPrefix = uploadFile.getOriginalFilename()
//                            .substring(0,uploadFile.getOriginalFilename().indexOf("."));
                    byte[] fileBuff = IOUtils.toByteArray(uploadFile.getInputStream());
                    String fileName = qiNiuYunUtil.uploadFile(fileBuff, UUID.randomUUID().toString() + prefix);
                    result.put(uploadFile.getOriginalFilename(),fileName);
//                    String sql = "update t_bs_bank set backbankimg = '" + fileName
//                            + "' where wdenable = 1 and bankname='" + fileNameWithoutPrefix + "';\t";
//                    System.out.println(sql);
                } catch (Exception e) {
                    log.error("getMultiImageUrl==error==" + e);
                    continue;
                }
            }
        }
        return result;
    }

    public void accountExportExcel(OprAdv oprAdv, HttpServletResponse response) {
        String fileName = "广告管理" + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ".xls";
        // OprAdv oprAdv = new Gson().fromJson(oprAdv,OprAdv.class);
        List<OprAdv> advList = oprAdvMapper.queryOprAdvList(oprAdv);
        List<Map<String, Object>> list = Lists.newArrayList();
        advList.stream().forEach(adv -> {
            Map<String, Object> paramr = new HashMap<>(8);
            paramr.put("title", adv.getTitle());
            paramr.put("advType", getAdvType(adv.getAdvType(), adv.getAdvTypeChild()));
            paramr.put("advClient", getClient(adv.getClientShow()));
            paramr.put("advTypeChildNum", adv.getAdvTypeChildNum());
            paramr.put("validity", adv.getUseStart() + "~" + adv.getUseEnd());
            paramr.put("creater", adv.getCreater());
            paramr.put("createTime", adv.getCreateTime());
        });
        Workbook workbook = ExcelUtil.commonExcelExportList("mapList", advExcelPath, list);
        try {
            ExcelUtil.writeExcel(response, workbook, fileName);
        } catch (IOException e) {
            throw new RRException(e.getMessage());
        }
    }

    public String getAdvType(Integer type, Integer typeChild) {
        String str = new String();
        if (type.equals(AdvConstant.ADV_ACROUSEL)) {
            str = "轮播图";
            switch (typeChild) {
                case Constants.EVNumber.one:
                    str = str + ">首页";
                    break;
                case Constants.EVNumber.two:
                    str = str + ">真人";
                    break;
                case Constants.EVNumber.three:
                    str = str + ">电子";
                    break;
                case Constants.EVNumber.four:
                    str = str + ">体育";
                    break;
                case Constants.EVNumber.five:
                    str = str + ">彩票";
                    break;
                default:

            }
        } else if (type.equals(AdvConstant.ADV_LEFT)) {
            str = "左对联";
        } else if (type.equals(AdvConstant.ADV_RIGHT)) {
            str = "右对联";
        } else if (type.equals(AdvConstant.ADV_BONUS_BANNER)) {
            str = "优惠页面banner";
        }
        return str;
    }

    public String getClient(Integer client) {
        String str = new String();
        if (client.equals(AdvConstant.CLIENT_PC)) {
            str = "PC端";
        } else if (client.equals(AdvConstant.CLIENT_MB)) {
            str = "移动端";
        } else if (client.equals(AdvConstant.CLIENT_PC_MB)) {
            str = "PC端、移动端";
        }
        return str;
    }

    public void enableOprAdv(OprAdv oprAdv, String userName, String ip) {
        if(1==oprAdv.getAvailable()){
            List<OprAdv> queryOprAdvList = oprAdvMapper.queryOprAdvByAvailable(oprAdv.getAdvType());
            if (queryOprAdvList != null && queryOprAdvList.size() >= Constants.EVNumber.one) {
                if((1 == oprAdv.getAdvType() || 2==oprAdv.getAdvType())){
                    throw new R200Exception("同一类型不能超过1条数据！");
                }
            }
        }
        oprAdvMapper.updateOprAdvAvailable(oprAdv);
        mbrAccountLogService.updateOprAdvStatusLog(oprAdv, userName, ip);
    }

    public OprAdv queryOprAdvInfo(Integer id) {
        OprAdv oprAdv = oprAdvMapper.queryOprAdvInfo(id);
        return oprAdv;
    }

    /**
     * 获取轮播图
     * @param bannerDto
     * @return
     */
    public List<AdvBanner> queryBannerList(AdvBanner bannerDto) {
        List<AdvBanner> resultList = new ArrayList<AdvBanner>();
        OprAdv oprAdv = new OprAdv();
        oprAdv.setAdvTypeChild(bannerDto.getAdvType());
        oprAdv.setClientShow(bannerDto.getClientShow());
        oprAdv.setAdvType(Constants.EVNumber.zero);  //轮播图
        List<OprAdv> oprAdvList = oprAdvMapper.queryWebOprAdvList(oprAdv);
        if (null!=oprAdvList && oprAdvList.size()>0) {
            for (OprAdv oprAdvObj : oprAdvList) {
                List<OprAdvImage> imageList = oprAdvObj.getImageList();
                for (OprAdvImage imageItem : imageList) {
                    AdvBanner banner = new AdvBanner();
                    banner.setClientShow(oprAdvObj.getClientShow());
                    banner.setPicTarget(imageItem.getPicTarget());
                    banner.setActId(imageItem.getActId());
                    banner.setActivityId(imageItem.getActivityId());
                    banner.setInType(imageItem.getInType());
                    banner.setInPageType(imageItem.getInPageType());
                    if (imageItem.getOutStation() != null && imageItem.getOutStation().length() > Constants.EVNumber.seven) {//校是否输入连接：http://
                        banner.setOutStation(imageItem.getOutStation());
                    }
                    banner.setPicPcPath(imageItem.getPcPath());
                    banner.setPicMbPath(imageItem.getMbPath());
                    resultList.add(banner);
                }
            }
        } else {    // 查询默认轮播图
            resultList = oprAdvMapper.queryAdvBannerDtoList(bannerDto);
        }
        return resultList;
    }

    /**
     * 获取弹窗列表
     * @param bannerDto
     * @return
     */
    public List<AdvBanner> queryPopUpList(AdvBanner bannerDto) {
        List<AdvBanner> resultList = new ArrayList<AdvBanner>();
        OprAdv oprAdv = new OprAdv();
        oprAdv.setClientShow(bannerDto.getClientShow());
        oprAdv.setAdvType(Constants.EVNumber.three);  // 弹窗
        List<OprAdv> oprAdvList = oprAdvMapper.queryWebOprAdvList(oprAdv);
        if (null!=oprAdvList && oprAdvList.size()>0) {
            for (OprAdv oprAdvObj : oprAdvList) {
                List<OprAdvImage> imageList = oprAdvObj.getImageList();
                for (OprAdvImage imageItem : imageList) {
                    AdvBanner banner = new AdvBanner();

                    banner.setTitle(oprAdvObj.getTitle());  // 标题
                    banner.setClientShow(oprAdvObj.getClientShow());
                    banner.setPicTarget(imageItem.getPicTarget());
                    banner.setActId(imageItem.getActId());
                    banner.setActivityId(imageItem.getActivityId());
                    banner.setInType(imageItem.getInType());
                    banner.setInPageType(imageItem.getInPageType());
                    if (imageItem.getOutStation() != null && imageItem.getOutStation().length() > Constants.EVNumber.seven) {//校是否输入连接：http://
                        banner.setOutStation(imageItem.getOutStation());
                    }
                    banner.setPicPcPath(imageItem.getPcPath());
                    banner.setPicMbPath(imageItem.getMbPath());
                    resultList.add(banner);
                }
            }
        }
        return resultList;
    }

    /**
     * 获取优惠页banner
     * @return
     */
    public List<AdvBanner> queryYouhuiBannerList() {
        List<AdvBanner> resultList = new ArrayList<AdvBanner>();
        OprAdv oprAdv = new OprAdv();
        oprAdv.setClientShow(Constants.EVNumber.zero);
        oprAdv.setAdvType(Constants.EVNumber.four);  //轮播图
        List<OprAdv> oprAdvList = oprAdvMapper.queryWebOprAdvList(oprAdv);
        if (null!=oprAdvList && oprAdvList.size()>0) {
            for (OprAdv oprAdvObj : oprAdvList) {
                List<OprAdvImage> imageList = oprAdvObj.getImageList();
                for (OprAdvImage imageItem : imageList) {
                    AdvBanner banner = new AdvBanner();
                    banner.setClientShow(oprAdvObj.getClientShow());
                    banner.setPicTarget(imageItem.getPicTarget());
                    banner.setActId(imageItem.getActId());
                    banner.setActivityId(imageItem.getActivityId());
                    banner.setInType(imageItem.getInType());
                    banner.setInPageType(imageItem.getInPageType());
                    if (imageItem.getOutStation() != null && imageItem.getOutStation().length() > Constants.EVNumber.seven) {//校是否输入连接：http://
                        banner.setOutStation(imageItem.getOutStation());
                    }
                    banner.setPicPcPath(imageItem.getPcPath());
                    banner.setPicMbPath(imageItem.getMbPath());
                    resultList.add(banner);
                }
            }
        }
        return resultList;
    }


    public List<OprAdv> coupletList() {
        List<OprAdv> resultList = oprAdvMapper.coupletList();
        return resultList;
    }

//-----------帮助中心--------------------------------------

    public PageUtils queryCategoryListPage(OprHelpCategory oprHelpCategory, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        oprHelpCategory.setIsdelete(Constants.Isdelete.enable);
        List<OprHelpCategory> list = oprHelpCategoryMapper.select(oprHelpCategory);
        PageUtils page = BeanUtil.toPagedResult(list);
        return page;
    }

    @Transactional
    public void saveHelpCategory(OprHelpCategory oprHelpCategory, String username, String ip) {
        oprHelpCategory.setCreater(username);
        oprHelpCategory.setIsdelete(Constants.Isdelete.enable);
        oprHelpCategory.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        oprHelpCategoryMapper.insert(oprHelpCategory);
        //添加操作日志
        mbrAccountLogService.addHelpCategoryLog(oprHelpCategory, username, ip);
    }

    @Transactional
    public void updateHelpCategory(OprHelpCategory oprHelpCategory, String username, String ip) {
        oprHelpCategory.setUpdater(username);
        oprHelpCategory.setUpdateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));

        oprHelpCategoryMapper.updateByPrimaryKeySelective(oprHelpCategory);

        //添加分类修改日志
        mbrAccountLogService.editHelpCategoryLog(oprHelpCategory, username, ip);
    }

    public void enableCategory(OprHelpCategory oprHelpCategory, String userName, String ip) {

        oprHelpCategoryMapper.updatCategoryAvailable(oprHelpCategory);
        mbrAccountLogService.updateCategoryStatusLog(oprHelpCategory, userName, ip);
    }

    public void deleteCategory(OprHelpCategory oprHelpCategory, String userName, String ip) {
        oprHelpCategory.setIsdelete(Constants.Isdelete.disable);
        oprHelpCategoryMapper.deleteCategory(oprHelpCategory);
      mbrAccountLogService.deleteCategoryLog(oprHelpCategory, userName, ip);

    }

    public OprHelpCategory queryCategoryInfo(Integer id) {

        OprHelpCategory oprHelpCategory = oprHelpCategoryMapper.selectByPrimaryKey(id);
        return oprHelpCategory;
    }


    public PageUtils queryTitleListPage(OprHelpTitle oprHelpTitle, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        oprHelpTitle.setIsdelete(Constants.Isdelete.enable);
        List<OprHelpTitle> list = oprHelpTitleMapper.select(oprHelpTitle);
        PageUtils page = BeanUtil.toPagedResult(list);
        return page;
    }

    public OprHelpTitle queryTitleInfo(Integer id) {
        OprHelpTitle oprHelpTitle = oprHelpTitleMapper.selectByPrimaryKey(id);
        return oprHelpTitle;
    }

    @Transactional
    public void saveHelpTitle(OprHelpTitle oprHelpTitle, String username, String ip) {
        oprHelpTitle.setCreater(username);
        oprHelpTitle.setIsdelete(Constants.Isdelete.enable);
        oprHelpTitle.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        oprHelpTitleMapper.insert(oprHelpTitle);
        //添加操作日志
        mbrAccountLogService.addHelpTitleLog(oprHelpTitle, username, ip);
    }

    @Transactional
    public void updateHelpTitle(OprHelpTitle oprHelpTitle, String username, String ip) {
        oprHelpTitle.setUpdater(username);
        oprHelpTitle.setUpdateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));

        oprHelpTitleMapper.updateByPrimaryKeySelective(oprHelpTitle);

        //添加分类修改日志
        mbrAccountLogService.editHelpTitleLog(oprHelpTitle, username, ip);
    }

//    public void enableTitle(OprHelpTitle oprHelpTitle, String userName, String ip) {
//        oprHelpTitleMapper.updatTitleAvailable(oprHelpTitle);
//        mbrAccountLogService.updateTitleStatusLog(oprHelpTitle, userName, ip);
//    }

    public void deleteTitle(OprHelpTitle oprHelpTitle, String userName, String ip) {
        oprHelpTitle.setIsdelete(Constants.Isdelete.disable);
        oprHelpTitleMapper.deleteTitle(oprHelpTitle);
        mbrAccountLogService.deleteTitleLog(oprHelpTitle, userName, ip);

    }

    public List<OprHelpCategory> findCategory(OprHelpCategory oprHelpCategory) {
        oprHelpCategory.setIsdelete(Constants.Isdelete.enable);
        oprHelpCategory.setAvailable(Constants.Available.enable);
        List<OprHelpCategory> list = oprHelpCategoryMapper.selectCategoryList(oprHelpCategory);
        return list;
    }

    public PageUtils queryContentListPage(OprHelpContent oprHelpContent, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        oprHelpContent.setIsdelete(Constants.Isdelete.enable);
        List<OprHelpContent> list = oprHelpContentMapper.select(oprHelpContent);
        PageUtils page = BeanUtil.toPagedResult(list);
        return page;
    }
    public List<OprHelpTitle> findTitle(OprHelpTitle oprHelpTitle) {
        oprHelpTitle.setIsdelete(Constants.Isdelete.enable);

        List<OprHelpTitle> list = oprHelpTitleMapper.select(oprHelpTitle);
        return list;
    }

    public OprHelpContent queryContetInfo(Integer id) {
        OprHelpContent oprHelpContent = oprHelpContentMapper.selectByPrimaryKey(id);
        return oprHelpContent;
    }

    @Transactional
    public void saveHelpContent(OprHelpContent oprHelpContent, String username, String ip) {
        oprHelpContent.setCreater(username);
        oprHelpContent.setIsdelete(Constants.Isdelete.enable);
        oprHelpContent.setCreateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));
        oprHelpContentMapper.insert(oprHelpContent);
        //添加操作日志
        mbrAccountLogService.addHelpContentLog(oprHelpContent, username, ip);
    }

    @Transactional
    public void updateHelpContent(OprHelpContent oprHelpContent, String username, String ip) {
        oprHelpContent.setUpdater(username);
        oprHelpContent.setUpdateTime(DateUtil.getCurrentDate(DateUtil.FORMAT_18_DATE_TIME));

        oprHelpContentMapper.updateByPrimaryKeySelective(oprHelpContent);

        //添加内容修改日志
        mbrAccountLogService.editHelpContentLog(oprHelpContent,username, ip);
    }

    public void deleteContent(OprHelpContent oprHelpContent, String userName, String ip) {
        oprHelpContent.setIsdelete(Constants.Isdelete.disable);
        oprHelpContentMapper.deleteContent(oprHelpContent);
        mbrAccountLogService.deleteContentLog(oprHelpContent, userName, ip);

    }
    public  List<OprHelpTitle> findTitleAndContent(OprHelpCategory oprHelpCategory) {
        //OprHelpCategory category = oprHelpCategoryMapper.selectOne(oprHelpCategory);
        OprHelpTitle title = new OprHelpTitle();
        title.setHelpCategoryId(oprHelpCategory.getId());
        title.setIsdelete(Constants.Isdelete.enable);

        List<OprHelpTitle> oprHelpTitles = oprHelpTitleMapper.select(title);
        OprHelpContent content = new OprHelpContent();
        if (null!=oprHelpTitles&&oprHelpTitles.size()>0){
            for (OprHelpTitle t:oprHelpTitles) {
                content.setHelpTitleId(t.getId());
                content.setIsdelete(Constants.Isdelete.enable);
                List<OprHelpContent> oprHelpContents = oprHelpContentMapper.select(content);
                t.setOprHelpContentList(oprHelpContents);
            }
            return oprHelpTitles;
        }
        return null;
    }
}
