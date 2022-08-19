package com.wsdy.saasops.modules.base.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

import java.util.Set;

/**
 * Created by William on 2017/11/24.
 */
@Slf4j
public class AuthorityProvider extends MapperTemplate {


    private final static String groupAuth ="IN (${baseAuth.groupIds}) ";

    private final static String agyAuth ="IN ( ${agyAccountIds} )";

    public AuthorityProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    public String selectInAuth(MappedStatement ms){
        Class<?> entityClass = getEntityClass(ms);
        //修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.selectAllColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        String tableName =tableName(entityClass);
        if(!"mbr_group".equals(tableName)){
            sql.append(SqlHelper.whereAllIfColumns(entityClass, isNotEmpty()));
        }else {
            sql.append(whereAllIfColumns(entityClass, isNotEmpty(), tableName(entityClass)));
        }
        sql.append(SqlHelper.orderByDefault(entityClass));
        return sql.toString();

    }

    private static String generateAuthSql(String tableName){

        return "mbr_group".equals(tableName)?"<if test=\" baseAuth.groupIds !='' and  baseAuth.groupIds != null \"> AND ID "+groupAuth+"</if>":null;
    }

    private static String whereAllIfColumns(Class<?> entityClass, boolean empty,String tableName){
        StringBuilder sql = new StringBuilder();
        sql.append("<where> 1=1");
        //获取全部列
        Set<EntityColumn> columnList = EntityHelper.getColumns(entityClass);
        //当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            sql.append(SqlHelper.getIfNotNull(column, " AND " + column.getColumnEqualsHolder(), empty));
        }
        //sql.append(generateAuthSql(tableName));
        //生成列之后吧权限的验证放入
        sql.append("</where>");
        //log.debug(sql.toString());
        return sql.toString();
    }

}
