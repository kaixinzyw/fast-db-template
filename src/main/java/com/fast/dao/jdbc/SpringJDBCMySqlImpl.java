package com.fast.dao.jdbc;

import cn.hutool.core.bean.BeanUtil;
import com.fast.config.PrimaryKeyType;
import com.fast.dao.DaoActuator;
import com.fast.dao.utils.*;
import com.fast.example.FastDaoParam;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.List;
import java.util.Objects;

/**
 * SpringJDBC NamedParameterJdbcTemplate执行器实现
 *
 * @author 张亚伟 https://github.com/kaixinzyw
 */
public class SpringJDBCMySqlImpl<T> implements DaoActuator<T> {

    @Override
    public Integer insert(T pojo) {
        FastDaoParam<T> param = FastDaoParam.init();
        param.setPojo(pojo);
        FastInsertProvider.insert(param);
        String jdbcSql = param.getSql().replaceAll("[#][{](paramMap.)(\\w*)[}]", ":$2");
        int insertCount;
        if (param.getTableMapper().getPrimaryKeyType().equals(PrimaryKeyType.AUTO)) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            insertCount = SpringJDBCMySqlDBConnection.getJdbcTemplate().update(jdbcSql, new MapSqlParameterSource(param.getParamMap()), keyHolder);
            BeanUtil.setFieldValue(pojo, param.getTableMapper().getPrimaryKeyField(), Objects.requireNonNull(keyHolder.getKey()).longValue());
        } else {
            insertCount = SpringJDBCMySqlDBConnection.getJdbcTemplate().update(jdbcSql, param.getParamMap());
        }
        FastSqlUtil.printSql(param, pojo);
        return insertCount;

    }

    @Override
    public List<T> findAll() {
        FastDaoParam<T> param = FastDaoParam.init();
        FastSelectProvider.findAll(param);
        String jdbcSql = param.getSql().replaceAll("[#][{](paramMap.)(\\w*)[}]", ":$2");
        List<T> query = SpringJDBCMySqlDBConnection.getJdbcTemplate().query(jdbcSql, param.getParamMap(), SpringJDBCMySqlRowMapper.init(param));
        FastSqlUtil.printSql(param, query);
        return query;
    }

    @Override
    public Integer findCount() {
        FastDaoParam<T> param = FastDaoParam.init();
        FastSelectProvider.findCount(param);
        String jdbcSql = param.getSql().replaceAll("[#][{](paramMap.)(\\w*)[}]", ":$2");
        try {
            Integer count = SpringJDBCMySqlDBConnection.getJdbcTemplate().queryForObject(jdbcSql, param.getParamMap(), Integer.class);
            FastSqlUtil.printSql(param, count);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer update(T pojo, boolean isSelective) {
        FastDaoParam<T> param = FastDaoParam.init();
        param.setPojo(pojo);
        param.setSelective(isSelective);
        FastUpdateProvider.update(param);
        String jdbcSql = param.getSql().replaceAll("[#][{](paramMap.)(\\w*)[}]", ":$2");
        int updateCount = SpringJDBCMySqlDBConnection.getJdbcTemplate().update(jdbcSql, param.getParamMap());
        FastSqlUtil.printSql(param, updateCount);
        return updateCount;
    }

    @Override
    public Integer delete() {
        FastDaoParam<T> param = FastDaoParam.init();
        FastDeleteProvider.delete(param);
        String jdbcSql = param.getSql().replaceAll("[#][{](paramMap.)(\\w*)[}]", ":$2");
        int delCount = SpringJDBCMySqlDBConnection.getJdbcTemplate().update(jdbcSql, param.getParamMap());
        FastSqlUtil.printSql(param, delCount);
        return delCount;
    }

}