package com.wsdy.saasops.modules.member.mapper;

import com.wsdy.saasops.api.modules.user.dto.FriendsListDto;
import com.wsdy.saasops.modules.member.dto.MbrFriendTransDetailDto;
import com.wsdy.saasops.modules.member.entity.MbrFriendTransDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MbrFriendTransMapper {

    List<FriendsListDto> findMbrFriendsList(Integer accoutId);


    List<MbrFriendTransDetail> findMbrFriendsTransList(MbrFriendTransDetailDto mbrFreindTransDetailDto);

    MbrFriendTransDetail findTodayCount(MbrFriendTransDetailDto mbrFreindTransDetailDto);

    MbrFriendTransDetail findFriendsTransOneInfo(@Param("mbdId") Integer mbdId);
}
