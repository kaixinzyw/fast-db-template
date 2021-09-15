package com.fast.dao.many;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fast.condition.ConditionPackages;
import com.fast.condition.FastExample;
import com.fast.fast.FastDao;
import com.fast.fast.FastDaoParam;
import com.fast.mapper.TableMapper;

import java.util.*;

/**
 * 多表查询工具类
 *
 * @author zyw
 */
public class ManyQuery {
    
    
    public static void relatedQuery(List parseArray, FastDaoParam fastDaoParam){
        if (CollUtil.isEmpty(parseArray)||!fastDaoParam.getConditionPackages().getRelatedQuery()) {
            return;
        }
        TableMapper tableMapper = fastDaoParam.getTableMapper();
        manyToMany(parseArray, tableMapper);
        oneToMany(parseArray, tableMapper);
        manyToOne(parseArray, tableMapper);
    }

    private static void manyToOne(List parseArray, TableMapper tableMapper) {
        List<ManyToOneInfo> manyToOneInfoList = tableMapper.getManyToOneInfoList();
        if (CollUtil.isNotEmpty(manyToOneInfoList)) {
            for (ManyToOneInfo manyToOneInfo : manyToOneInfoList) {
                Set<Object> manyPrimaryKeyList = new HashSet<>();
                for (Object t : parseArray) {
                    Object fieldValue = BeanUtil.getFieldValue(t, manyToOneInfo.getJoinMapperFieldName());
                    if (fieldValue != null) {
                        manyPrimaryKeyList.add(fieldValue);
                    }
                }
                ConditionPackages conditionPackages = ConditionPackages.create(manyToOneInfo.getJoinMapper().getObjClass());
                conditionPackages.addInQuery(manyToOneInfo.getJoinMapper().getPrimaryKeyField(),manyPrimaryKeyList);
                List oneDataList = FastDao.init(conditionPackages).findAll();
                if (CollUtil.isNotEmpty(oneDataList)) {
                    Map<Object, Object> manyDataPrimaryKeyGroup = new HashMap<>();
                    for (Object manyData : oneDataList) {
                        Object key = BeanUtil.getFieldValue(manyData, manyToOneInfo.getJoinMapper().getPrimaryKeyField());
                        manyDataPrimaryKeyGroup.put(key,manyData);
                    }
                    for (Object t : parseArray) {
                        Object manyPrimaryKey = BeanUtil.getFieldValue(t, manyToOneInfo.getJoinMapperFieldName());
                        Object ts = manyDataPrimaryKeyGroup.get(manyPrimaryKey);
                        if (ObjectUtil.isNotNull(ts)) {
                            BeanUtil.setFieldValue(t,manyToOneInfo.getDataFieldName(),ts);
                        }
                    }
                }
            }
        }
    }

    private static void oneToMany(List parseArray, TableMapper tableMapper) {
        Set<Object> primaryKeyList = new HashSet<>();
        for (Object t : parseArray) {
            Object fieldValue = BeanUtil.getFieldValue(t, tableMapper.getPrimaryKeyField());
            if (fieldValue != null) {
                primaryKeyList.add(fieldValue);
            }
        }
        List<OneToManyInfo> oneToManyInfoList = tableMapper.getOneToManyInfoList();
        if (CollUtil.isNotEmpty(oneToManyInfoList)) {
            for (OneToManyInfo oneToManyInfo : oneToManyInfoList) {

                ConditionPackages conditionPackages = ConditionPackages.create(oneToManyInfo.getJoinMapper().getObjClass());
                conditionPackages.addInQuery(oneToManyInfo.getJoinMapperFieldName(),primaryKeyList);
                List manyDataList = FastDao.init(conditionPackages).findAll();
                if (CollUtil.isNotEmpty(manyDataList)) {
                    Map<Object, List<Object>> manyDataPrimaryKeyGroup = new HashMap<>();
                    for (Object manyData : manyDataList) {
                        Object key = BeanUtil.getFieldValue(manyData, oneToManyInfo.getJoinMapperFieldName());
                        List group = manyDataPrimaryKeyGroup.get(key);
                        if (CollUtil.isEmpty(group)) {
                            group = new ArrayList<>();
                            manyDataPrimaryKeyGroup.put(key, group);
                        }
                        group.add(manyData);
                    }

                    for (Object t : parseArray) {
                        Object manyPrimaryKey = BeanUtil.getFieldValue(t, tableMapper.getPrimaryKeyField());
                        List<Object> ts = manyDataPrimaryKeyGroup.get(manyPrimaryKey);
                        if (CollUtil.isNotEmpty(ts)) {
                            BeanUtil.setFieldValue(t,oneToManyInfo.getDataFieldName(),ts);
                        }
                    }
                }
            }
        }
    }

    private static void manyToMany(List parseArray, TableMapper tableMapper) {
        Set<Object> primaryKeyList = new HashSet<>();
        for (Object t : parseArray) {
            Object fieldValue = BeanUtil.getFieldValue(t, tableMapper.getPrimaryKeyField());
            if (fieldValue != null) {
                primaryKeyList.add(fieldValue);
            }
        }
        List<ManyToManyInfo> manyToManyInfoList = tableMapper.getManyToManyInfoList();
        if (CollUtil.isNotEmpty(manyToManyInfoList)) {
            for (ManyToManyInfo manyToManyInfo : manyToManyInfoList) {
                ConditionPackages conditionPackages = ConditionPackages.create(manyToManyInfo.getJoinMapper().getObjClass());
                conditionPackages.addInQuery(manyToManyInfo.getJoinMapperFieldName(),primaryKeyList);
                List middleDataList = FastDao.init(conditionPackages).findAll();
                if (CollUtil.isNotEmpty(middleDataList)) {
                    Set<Object> toManyPrimaryKeyList = new HashSet<>();
                    for (Object middleData : middleDataList) {
                        Object fieldValue = BeanUtil.getFieldValue(middleData, manyToManyInfo.getRelationMapperFieldName());
                        if (fieldValue != null) {
                            toManyPrimaryKeyList.add(fieldValue);
                        }
                    }
                    if (CollUtil.isNotEmpty(toManyPrimaryKeyList)) {
                        ConditionPackages packages = ConditionPackages.create(manyToManyInfo.getRelationMapper().getObjClass());
                        packages.addInQuery(manyToManyInfo.getRelationMapper().getPrimaryKeyTableField(),toManyPrimaryKeyList);
                        List toManyDataList = FastDao.init(packages).findAll();
                        if (CollUtil.isNotEmpty(toManyDataList)) {
                            Map<Object, List<Object>> middleDataPrimaryKeyGroup = new HashMap<>();
                            for (Object t : middleDataList) {
                                Object key = BeanUtil.getFieldValue(t, manyToManyInfo.getJoinMapperFieldName());
                                List vs = middleDataPrimaryKeyGroup.get(key);
                                if (CollUtil.isEmpty(vs)) {
                                    vs = new ArrayList<>();
                                    middleDataPrimaryKeyGroup.put(key, vs);
                                }
                                vs.add(BeanUtil.getFieldValue(t, manyToManyInfo.getRelationMapperFieldName()));
                            }
                            Map<Object, List<Object>> middleDataGroup = new HashMap<>();
                            for (Object primaryKey : middleDataPrimaryKeyGroup.keySet()) {
                                List<Object> toManyPrimaryKeyDataList = middleDataPrimaryKeyGroup.get(primaryKey);
                                List<Object> toManyDataGroupList = new ArrayList<>();
                                for (Object toManyPrimaryKey : toManyPrimaryKeyDataList) {
                                    for (Object toManyData : toManyDataList) {
                                        if (ObjectUtil.equal(BeanUtil.getFieldValue(toManyData, manyToManyInfo.getRelationMapper().getPrimaryKeyField()), toManyPrimaryKey)) {
                                            toManyDataGroupList.add(toManyData);
                                            break;
                                        }
                                    }
                                }
                                middleDataGroup.put(primaryKey, toManyDataGroupList);
                            }
                            for (Object t : parseArray) {
                                Object manyPrimaryKey = BeanUtil.getFieldValue(t, tableMapper.getPrimaryKeyField());
                                List<Object> ts = middleDataGroup.get(manyPrimaryKey);
                                if (CollUtil.isNotEmpty(ts)) {
                                    BeanUtil.setFieldValue(t,manyToManyInfo.getDataFieldName(),ts);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}