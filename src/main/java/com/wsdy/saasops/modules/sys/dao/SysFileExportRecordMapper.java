package com.wsdy.saasops.modules.sys.dao;


import com.wsdy.saasops.modules.base.mapper.MyMapper;
import com.wsdy.saasops.modules.sys.entity.SysFileExportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysFileExportRecordMapper extends MyMapper<SysFileExportRecord> {

    SysFileExportRecord getAsynFileExportRecordByUserId(@Param("userId") Long userId, @Param("module") String module);

    int updateFileId(@Param("userId") Long userId, @Param("module") String module, @Param("fileId") String fileId);

}
