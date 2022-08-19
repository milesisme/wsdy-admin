package com.wsdy.saasops.modules.member.dao;

        import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;
        import org.apache.ibatis.annotations.Mapper;
        import com.wsdy.saasops.modules.base.mapper.MyMapper;
        import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface MbrFriendTransDetailMapper extends MyMapper<MbrFriendTransDetail>,IdsMapper<MbrFriendTransDetail> {

}
