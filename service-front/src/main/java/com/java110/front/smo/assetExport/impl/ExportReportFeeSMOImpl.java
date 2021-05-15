package com.java110.front.smo.assetExport.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.java110.core.component.BaseComponentSMO;
import com.java110.core.context.IPageData;
import com.java110.dto.fee.FeeConfigDto;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.front.smo.assetExport.IExportReportFeeSMO;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AssetImportSmoImpl
 * @Description TODO
 * @Author wuxw
 * @Date 2019/9/23 23:14
 * @Version 1.0
 * add by wuxw 2019/9/23
 **/
@Service("exportReportFeeSMOImpl")
public class ExportReportFeeSMOImpl extends BaseComponentSMO implements IExportReportFeeSMO {
    private final static Logger logger = LoggerFactory.getLogger(ExportReportFeeSMOImpl.class);
    public static final String REPORT_FEE_SUMMARY = "reportFeeSummary";
    public static final String REPORT_FLOOR_UNIT_FEE_SUMMARY = "reportFloorUnitFeeSummary";
    public static final String REPORT_FEE_BREAKDOWN = "reportFeeBreakdown";
    public static final String REPORT_FEE_DETAIL = "reportFeeDetail";
    public static final String REPORT_OWE_FEE_DETAIL = "reportOweFeeDetail";
    public static final String REPORT_PAY_FEE_DETAIL = "reportPayFeeDetail";
    public static final String REPORT_YEAR_COLLECTION = "reportYearCollection";
    public static final String REPORT_LIST_OWE_FEE = "listOweFee";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseEntity<Object> exportExcelData(IPageData pd) throws Exception {

        ComponentValidateResult result = this.validateStoreStaffCommunityRelationship(pd, restTemplate);

        Assert.hasKeyAndValue(JSONObject.parseObject(pd.getReqData()), "communityId", "请求中未包含小区");
        Assert.hasKeyAndValue(JSONObject.parseObject(pd.getReqData()), "pagePath", "请求中未包含页面");

        Workbook workbook = null;  //工作簿
        //工作表
        workbook = new XSSFWorkbook();
        JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        String pagePath = reqJson.getString("pagePath");

        switch (pagePath) {
            case REPORT_FEE_SUMMARY:
                reportFeeSummary(pd, result, workbook);
                break;
            case REPORT_FLOOR_UNIT_FEE_SUMMARY:
                reportFloorUnitFeeSummary(pd, result, workbook);
                break;
            case REPORT_FEE_BREAKDOWN:
                reportFeeBreakdown(pd, result, workbook);
                break;
            case REPORT_FEE_DETAIL:
                reportFeeDetail(pd, result, workbook);
                break;

            case REPORT_OWE_FEE_DETAIL:
                reportOweFeeDetail(pd, result, workbook);
                break;
            case REPORT_PAY_FEE_DETAIL:
                reportPayFeeDetail(pd, result, workbook);
                break;
            case REPORT_YEAR_COLLECTION:
                reportYearCollection(pd, result, workbook);
                break;
            case REPORT_LIST_OWE_FEE:
                reportListOweFee(pd, result, workbook);
                break;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        MultiValueMap headers = new HttpHeaders();
        headers.add("content-type", "application/octet-stream;charset=UTF-8");
        headers.add("Content-Disposition", "attachment;filename=" + pagePath + DateUtil.getyyyyMMddhhmmssDateString() + ".xlsx");
        headers.add("Pargam", "no-cache");
        headers.add("Cache-Control", "no-cache");
        //headers.add("Content-Disposition", "attachment; filename=" + outParam.getString("fileName"));
        headers.add("Accept-Ranges", "bytes");
        byte[] context = null;
        try {
            workbook.write(os);
            context = os.toByteArray();
            os.close();
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
            // 保存数据
            return new ResponseEntity<Object>("导出失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        // 保存数据
        return new ResponseEntity<Object>(context, headers, HttpStatus.OK);
    }

    private void reportListOweFee(IPageData pd, ComponentValidateResult result, Workbook workbook) {
        JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        String configIds = reqJson.getString("configIds");
        //查询楼栋信息
        JSONArray oweFees = this.getReportListOweFees(pd, result);
        if (oweFees == null) {
            return;
        }
        //获取费用项
        List<FeeConfigDto> feeConfigDtos = getFeeConfigs(oweFees);
        Sheet sheet = workbook.createSheet("欠费清单");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("收费对象");
        row.createCell(1).setCellValue("业主名称");
        row.createCell(2).setCellValue("开始时间");
        row.createCell(3).setCellValue("结束时间");
        if (!StringUtil.isEmpty(configIds)) {
            for (int feeConfigIndex = 0; feeConfigIndex < feeConfigDtos.size(); feeConfigIndex++) {
                row.createCell(4 + feeConfigIndex).setCellValue(feeConfigDtos.get(feeConfigIndex).getFeeName());
            }
            row.createCell(4 + feeConfigDtos.size()).setCellValue("合计");
        } else {
            row.createCell(4).setCellValue("合计");
        }


        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < oweFees.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = oweFees.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(dataObj.getString("payerObjName"));
            row.createCell(1).setCellValue(dataObj.getString("ownerName"));
            row.createCell(2).setCellValue(dataObj.getString("endTime"));
            row.createCell(3).setCellValue(dataObj.getString("deadlineTime"));
            if (!StringUtil.isEmpty(configIds)) {
                for (int feeConfigIndex = 0; feeConfigIndex < feeConfigDtos.size(); feeConfigIndex++) {
                    row.createCell(4 + feeConfigIndex).setCellValue(getFeeConfigAmount(feeConfigDtos.get(feeConfigIndex), dataObj));
                }
                row.createCell(4 + feeConfigDtos.size()).setCellValue(getAllFeeOweAmount(feeConfigDtos, dataObj));
            } else {
                row.createCell(4).setCellValue(getAllFeeOweAmount(feeConfigDtos, dataObj));
            }

        }
    }

    /**
     * _getAllFeeOweAmount: function (_fee) {
     * let _feeConfigNames = $that.listOweFeeInfo.feeConfigNames;
     * if (_feeConfigNames.length < 1) {
     * return _fee.amountOwed;
     * }
     * <p>
     * let _amountOwed = 0.0;
     * let _items = _fee.items;
     * _feeConfigNames.forEach(_feeItem =>{
     * _items.forEach(_item=>{
     * if(_feeItem.configId == _item.configId){
     * _amountOwed += parseFloat(_item.amountOwed);
     * }
     * })
     * })
     * return _amountOwed;
     * }
     *
     * @param dataObj
     * @return
     */
    private double getAllFeeOweAmount(List<FeeConfigDto> feeConfigDtos, JSONObject dataObj) {
        if (feeConfigDtos == null || feeConfigDtos.size() < 1) {
            return dataObj.getDouble("amountOwed");
        }
        JSONArray items = dataObj.getJSONArray("items");
        if (items == null || items.size() < 1) {
            return dataObj.getDouble("amountOwed");
        }

        BigDecimal oweAmount = new BigDecimal(0);
        for (FeeConfigDto feeConfigDto : feeConfigDtos) {
            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                if (feeConfigDto.getConfigId().equals(items.getJSONObject(itemIndex).getString("configId"))) {
                    oweAmount = oweAmount.add(new BigDecimal(items.getJSONObject(itemIndex).getDouble("amountOwed")));
                }
            }
        }

        return oweAmount.doubleValue();
    }

    private double getFeeConfigAmount(FeeConfigDto feeConfigDto, JSONObject dataObj) {
        JSONArray items = dataObj.getJSONArray("items");
        double oweAmount = 0;

        if (items == null || items.size() < 1) {
            return oweAmount;
        }

        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            if (feeConfigDto.getConfigId().equals(items.getJSONObject(itemIndex).getString("configId"))) {
                oweAmount = items.getJSONObject(itemIndex).getDouble("amountOwed");
                break;
            }
        }
        return oweAmount;
    }


    private List<FeeConfigDto> getFeeConfigs(JSONArray oweFees) {
        List<FeeConfigDto> feeConfigDtos = new ArrayList<>();
        FeeConfigDto feeConfigDto = null;
        for (int oweFeeIndex = 0; oweFeeIndex < oweFees.size(); oweFeeIndex++) {
            JSONArray items = oweFees.getJSONObject(oweFeeIndex).getJSONArray("items");
            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                if (existsFeeConfig(feeConfigDtos, items.getJSONObject(itemIndex))) {
                    continue;
                }
                feeConfigDto = new FeeConfigDto();
                feeConfigDto.setConfigId(items.getJSONObject(itemIndex).getString("configId"));
                feeConfigDto.setFeeName(items.getJSONObject(itemIndex).getString("configName"));
                feeConfigDtos.add(feeConfigDto);
            }
        }

        return feeConfigDtos;
    }

    private boolean existsFeeConfig(List<FeeConfigDto> feeConfigDtos, JSONObject oweFee) {
        if (feeConfigDtos.size() < 1) {
            return false;
        }
        for (FeeConfigDto feeConfigDto : feeConfigDtos) {
            if (feeConfigDto.getConfigId().equals(oweFee.getString("configId"))) {
                return true;
            }
        }

        return false;
    }


    private void reportPayFeeDetail(IPageData pd, ComponentValidateResult result, Workbook workbook) {
        Sheet sheet = workbook.createSheet("缴费明细表");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("费用编号");
        row.createCell(1).setCellValue("房号");
        row.createCell(2).setCellValue("费用项");
        row.createCell(3).setCellValue("支付方式");
        row.createCell(4).setCellValue("缴费开始时间");
        row.createCell(5).setCellValue("缴费结束时间");
        row.createCell(6).setCellValue("缴费时间");
        row.createCell(7).setCellValue("应收金额");
        row.createCell(8).setCellValue("实收金额");
        row.createCell(9).setCellValue("优惠金额");
        row.createCell(10).setCellValue("减免金额");
        row.createCell(11).setCellValue("滞纳金");
        row.createCell(12).setCellValue("空置房打折金额");
        row.createCell(13).setCellValue("空置房减免金额");
        //查询楼栋信息
        JSONArray rooms = this.getReportPayFeeDetail(pd, result);
        if (rooms == null) {
            return;
        }
        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = rooms.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(roomIndex + 1);
            row.createCell(1).setCellValue(dataObj.getString("objName"));
            row.createCell(2).setCellValue(dataObj.getString("feeName"));
            row.createCell(3).setCellValue(dataObj.getString("primeRate"));
            row.createCell(4).setCellValue(dataObj.getString("startTime"));
            row.createCell(5).setCellValue(dataObj.getString("endTime"));
            row.createCell(6).setCellValue(dataObj.getString("createTime"));
            row.createCell(7).setCellValue(dataObj.getString("receivableAmount"));
            row.createCell(8).setCellValue(dataObj.getString("receivedAmount"));
            row.createCell(9).setCellValue(dataObj.getString("preferentialAmount"));
            row.createCell(10).setCellValue(dataObj.getString("deductionAmount"));
            row.createCell(11).setCellValue(dataObj.getString("lateFee"));
            row.createCell(12).setCellValue(dataObj.getString("vacantHousingDiscount"));
            row.createCell(13).setCellValue(dataObj.getString("vacantHousingReduction"));
        }
    }

    private void reportYearCollection(IPageData pd, ComponentValidateResult result, Workbook workbook) {
        Sheet sheet = workbook.createSheet("费用台账");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("姓名");
        row.createCell(1).setCellValue("房号");
        row.createCell(2).setCellValue("联系电话");
        row.createCell(3).setCellValue("面积");
        row.createCell(4).setCellValue("费用名称");
        row.createCell(5).setCellValue("应收金额");

        //查询楼栋信息
        JSONArray rooms = this.getReportYearCollection(pd, result);
        if (rooms == null) {
            return;
        }
        JSONArray reportFeeYearCollectionDetailDtos = rooms.getJSONObject(0).getJSONArray("reportFeeYearCollectionDetailDtos");

        for (int detailIndex = 0; detailIndex < reportFeeYearCollectionDetailDtos.size(); detailIndex++) {
            row.createCell(6 + detailIndex).setCellValue(reportFeeYearCollectionDetailDtos.getJSONObject(detailIndex).getString("collectionYear") + "年");
        }

        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = rooms.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(dataObj.getString("ownerName"));
            row.createCell(1).setCellValue(dataObj.getString("objName"));
            row.createCell(2).setCellValue(dataObj.getString("ownerLink"));
            row.createCell(3).setCellValue(dataObj.getString("builtUpArea"));
            row.createCell(4).setCellValue(dataObj.getString("feeName"));
            row.createCell(5).setCellValue(dataObj.getString("receivableAmount"));

            reportFeeYearCollectionDetailDtos = dataObj.getJSONArray("reportFeeYearCollectionDetailDtos");
            for (int detailIndex = 0; detailIndex < reportFeeYearCollectionDetailDtos.size(); detailIndex++) {
                row.createCell(6 + detailIndex).setCellValue(reportFeeYearCollectionDetailDtos.getJSONObject(detailIndex).getString("receivedAmount"));
            }
        }
    }

    private JSONArray getReportListOweFees(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        reqJson.put("page", 1);
        reqJson.put("row", 10000);
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api//reportOweFee/queryReportAllOweFee" + mapToUrlParam(reqJson);
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    private JSONArray getReportPayFeeDetail(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        reqJson.put("page", 1);
        reqJson.put("row", 10000);
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api/reportFeeMonthStatistics/queryPayFeeDetail" + mapToUrlParam(reqJson);
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    private JSONArray getReportYearCollection(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        JSONObject reqJson = JSONObject.parseObject(pd.getReqData());
        reqJson.put("page", 1);
        reqJson.put("row", 10001);
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api/reportFeeYearCollection/queryReportFeeYear" + mapToUrlParam(reqJson);
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    private void reportOweFeeDetail(IPageData pd, ComponentValidateResult result, Workbook workbook) {
        Sheet sheet = workbook.createSheet("欠费明细表");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("费用编号");
        row.createCell(1).setCellValue("房号");
        row.createCell(2).setCellValue("费用项");
        row.createCell(3).setCellValue("费用开始时间");
        row.createCell(4).setCellValue("欠费时长（天）");
        row.createCell(5).setCellValue("欠费金额");
        //查询楼栋信息
        JSONArray rooms = this.getReportOweFeeDetail(pd, result);
        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = rooms.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(roomIndex + 1);
            row.createCell(1).setCellValue(dataObj.getString("objName"));
            row.createCell(2).setCellValue(dataObj.getString("feeName"));
            row.createCell(3).setCellValue(dataObj.getString("feeCreateTime"));
            row.createCell(4).setCellValue(dataObj.getString("oweDay"));
            row.createCell(5).setCellValue(dataObj.getString("oweAmount"));
        }
    }

    private void reportFeeDetail(IPageData pd, ComponentValidateResult result, Workbook workbook) {
        Sheet sheet = workbook.createSheet("费用明细表");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("费用编号");
        row.createCell(1).setCellValue("房号");
        row.createCell(2).setCellValue("费用项");
        row.createCell(3).setCellValue("费用开始时间");
        row.createCell(4).setCellValue("费用结束时间");
        row.createCell(5).setCellValue("应收金额");
        row.createCell(6).setCellValue("实收金额");
        //查询楼栋信息
        JSONArray rooms = this.getReportFeeDetail(pd, result);
        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = rooms.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(roomIndex + 1);
            row.createCell(1).setCellValue(dataObj.getString("objName"));
            row.createCell(2).setCellValue(dataObj.getString("feeName"));
            row.createCell(3).setCellValue(dataObj.getString("feeCreateTime"));
            row.createCell(4).setCellValue(dataObj.getString("deadlineTime"));
            row.createCell(5).setCellValue(dataObj.getString("receivableAmount"));
            row.createCell(6).setCellValue(dataObj.getString("receivedAmount"));
        }
    }

    private JSONArray getReportOweFeeDetail(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api/reportFeeMonthStatistics/queryOweFeeDetail?communityId=" + result.getCommunityId() + "&page=1&row=10000";
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    private JSONArray getReportFeeDetail(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api/reportFeeMonthStatistics/queryFeeDetail?communityId=" + result.getCommunityId() + "&page=1&row=10000";
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    private void reportFeeBreakdown(IPageData pd, ComponentValidateResult result, Workbook workbook) {
        Sheet sheet = workbook.createSheet("费用分项表");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("费用编号");
        row.createCell(1).setCellValue("费用类型");
        row.createCell(2).setCellValue("费用项");
        row.createCell(3).setCellValue("费用开始时间");
        row.createCell(4).setCellValue("应收金额");
        row.createCell(5).setCellValue("实收金额");
        row.createCell(6).setCellValue("欠费金额");
        //查询楼栋信息
        JSONArray rooms = this.getReportFeeBreakdown(pd, result);
        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = rooms.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(roomIndex + 1);
            row.createCell(1).setCellValue(dataObj.getString("feeTypeCd"));
            row.createCell(2).setCellValue(dataObj.getString("feeName"));
            row.createCell(3).setCellValue(dataObj.getString("feeCreateTime"));
            row.createCell(4).setCellValue(dataObj.getString("receivableAmount"));
            row.createCell(5).setCellValue(dataObj.getString("receivedAmount"));
            row.createCell(6).setCellValue(dataObj.getString("oweAmount"));
        }
    }

    private JSONArray getReportFeeBreakdown(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api/reportFeeMonthStatistics/queryFeeBreakdown?communityId=" + result.getCommunityId() + "&page=1&row=10000";
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    private void reportFloorUnitFeeSummary(IPageData pd, ComponentValidateResult result, Workbook workbook) {
        Sheet sheet = workbook.createSheet("楼栋费用表");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("楼栋");
        row.createCell(2).setCellValue("单元");
        row.createCell(3).setCellValue("应收金额");
        row.createCell(4).setCellValue("实收金额");
        row.createCell(5).setCellValue("欠费金额");
        //查询楼栋信息
        JSONArray rooms = this.getReportFloorUnitFeeSummary(pd, result);
        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = rooms.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(dataObj.getString("feeYear") + "年" + dataObj.getString("feeMonth") + "月");
            row.createCell(1).setCellValue(dataObj.getString("floorNum") + "栋");
            row.createCell(2).setCellValue(dataObj.getString("unitNum") + "单元");
            row.createCell(3).setCellValue(dataObj.getString("receivableAmount"));
            row.createCell(4).setCellValue(dataObj.getString("receivedAmount"));
            row.createCell(5).setCellValue(dataObj.getString("oweAmount"));
        }
    }

    private JSONArray getReportFloorUnitFeeSummary(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api/reportFeeMonthStatistics/queryFloorUnitFeeSummary?communityId=" + result.getCommunityId() + "&page=1&row=10000";
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    /**
     * 查询存在的房屋信息
     * room.queryRooms
     *
     * @param pd
     * @param result
     * @return
     */
    private JSONArray getReportFeeSummaryFee(IPageData pd, ComponentValidateResult result) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        apiUrl = ServiceConstant.SERVICE_API_URL + "/api/reportFeeMonthStatistics/queryReportFeeSummary?communityId=" + result.getCommunityId() + "&page=1&row=10000";
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);
        if (responseEntity.getStatusCode() != HttpStatus.OK) { //跳过 保存单元信息
            return null;
        }
        JSONObject savedRoomInfoResults = JSONObject.parseObject(responseEntity.getBody(), Feature.OrderedField);
        if (!savedRoomInfoResults.containsKey("data")) {
            return null;
        }
        return savedRoomInfoResults.getJSONArray("data");
    }

    /**
     * 获取 房屋信息
     *
     * @param componentValidateResult
     * @param workbook
     */
    private void reportFeeSummary(IPageData pd, ComponentValidateResult componentValidateResult, Workbook workbook) {
        Sheet sheet = workbook.createSheet("费用汇总表");
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("日期");
        row.createCell(1).setCellValue("应收金额");
        row.createCell(2).setCellValue("实收金额");
        row.createCell(3).setCellValue("欠费金额");
        //查询楼栋信息
        JSONArray rooms = this.getReportFeeSummaryFee(pd, componentValidateResult);
        JSONObject dataObj = null;
        for (int roomIndex = 0; roomIndex < rooms.size(); roomIndex++) {
            row = sheet.createRow(roomIndex + 1);
            dataObj = rooms.getJSONObject(roomIndex);
            row.createCell(0).setCellValue(dataObj.getString("feeYear") + "年" + dataObj.getString("feeMonth") + "月");
            row.createCell(1).setCellValue(dataObj.getString("receivableAmount"));
            row.createCell(2).setCellValue(dataObj.getString("receivedAmount"));
            row.createCell(3).setCellValue(dataObj.getString("oweAmount"));
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
