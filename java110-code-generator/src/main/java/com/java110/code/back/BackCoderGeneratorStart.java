package com.java110.code.back;


import com.alibaba.fastjson.JSONObject;
import com.java110.code.back.Data;
import com.java110.code.back.GeneratorAbstractBussiness;
import com.java110.code.back.GeneratorDeleteInfoListener;
import com.java110.code.back.GeneratorDtoBean;
import com.java110.code.back.GeneratorIInnerServiceSMO;
import com.java110.code.back.GeneratorIServiceDaoListener;
import com.java110.code.back.GeneratorInnerServiceSMOImpl;
import com.java110.code.back.GeneratorSaveInfoListener;
import com.java110.code.back.GeneratorServiceDaoImplListener;
import com.java110.code.back.GeneratorServiceDaoImplMapperListener;
import com.java110.code.back.GeneratorUpdateInfoListener;
import com.java110.code.web.GeneratorStart;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class BackCoderGeneratorStart extends BaseGenerator {

    protected BackCoderGeneratorStart() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /**
     * 代码生成器 入口方法
     *  此处生成的mapper文件包含过程表和实例表的sql,所以要求两张表的特殊字段也要写上
     *   BusinessTypeCd
     * @param args 参数
     */
    public static void main(String[] args) {

        //加载配置
        StringBuffer sb = readFile(GeneratorStart.class.getResource("/web/template_1.json").getFile());

        JSONObject data = JSONObject.parseObject(sb.toString());

        GeneratorSaveInfoListener generatorSaveInfoListener = new GeneratorSaveInfoListener();
        generatorSaveInfoListener.generator(data);

        GeneratorAbstractBussiness generatorAbstractBussiness = new GeneratorAbstractBussiness();
        generatorAbstractBussiness.generator(data);

        GeneratorIServiceDaoListener generatorIServiceDaoListener = new GeneratorIServiceDaoListener();
        generatorIServiceDaoListener.generator(data);

        GeneratorServiceDaoImplListener generatorServiceDaoImplListener = new GeneratorServiceDaoImplListener();
        generatorServiceDaoImplListener.generator(data);

        GeneratorServiceDaoImplMapperListener generatorServiceDaoImplMapperListener = null;
        generatorServiceDaoImplMapperListener = new GeneratorServiceDaoImplMapperListener();
        generatorServiceDaoImplMapperListener.generator(data);

        GeneratorUpdateInfoListener generatorUpdateInfoListener = new GeneratorUpdateInfoListener();
        generatorUpdateInfoListener.generator(data);

        GeneratorDeleteInfoListener generatorDeleteInfoListener = new GeneratorDeleteInfoListener();
        generatorDeleteInfoListener.generator(data);

        GeneratorInnerServiceSMOImpl generatorInnerServiceSMOImpl = new GeneratorInnerServiceSMOImpl();
        generatorInnerServiceSMOImpl.generator(data);

        GeneratorDtoBean generatorDtoBean = new GeneratorDtoBean();
        generatorDtoBean.generator(data);

        GeneratorIInnerServiceSMO generatorIInnerServiceSMO = new GeneratorIInnerServiceSMO();
        generatorIInnerServiceSMO.generator(data);
    }
}