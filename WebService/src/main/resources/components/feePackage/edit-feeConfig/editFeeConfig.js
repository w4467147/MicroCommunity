(function(vc, vm) {

    vc.extends({
        data: {
            editFeeConfigInfo: {
                configId: '',
                feeTypeCd: '',
                feeName: '',
                feeFlag: '',
                startTime: '',
                endTime: '',
                computingFormula: '',
                squarePrice: '',
                additionalAmount: '0.00',
                isDefault:'',
                feeTypeCds:[],
                computingFormulas:[]
            }
        },
        _initMethod: function() {
            vc.component._initEditFeeConfigDateInfo();
            vc.getDict('pay_fee_config',"fee_type_cd",function(_data){
                vc.component.editFeeConfigInfo.feeTypeCds = _data;
            });
            vc.getDict('pay_fee_config',"computing_formula",function(_data){
                vc.component.editFeeConfigInfo.computingFormulas = _data;
            });
        },
        _initEvent: function() {
            vc.on('editFeeConfig', 'openEditFeeConfigModal',
            function(_params) {
                vc.component.refreshEditFeeConfigInfo();
                $('#editFeeConfigModel').modal('show');
                vc.copyObject(_params, vc.component.editFeeConfigInfo);
                vc.component.editFeeConfigInfo.communityId = vc.getCurrentCommunity().communityId;
            });
        },
        methods: {
            _initEditFeeConfigDateInfo: function () {
                vc.component.editFeeConfigInfo.startTime = vc.dateFormat(new Date().getTime());
                $('.editFeeConfigStartTime').datetimepicker({
                    language: 'zh-CN',
                    format: 'yyyy-mm-dd hh:ii:ss',
                    initTime: true,
                    initialDate: new Date(),
                    autoClose: 1,
                    todayBtn: true

                });
                $('.editFeeConfigStartTime').datetimepicker()
                    .on('changeDate', function (ev) {
                        var value = $(".editFeeConfigStartTime").val();
                        vc.component.editFeeConfigInfo.startTime = value;
                    });
                $('.editFeeConfigEndTime').datetimepicker({
                    language: 'zh-CN',
                    format: 'yyyy-mm-dd hh:ii:ss',
                    initTime: true,
                    initialDate: new Date(),
                    autoClose: 1,
                    todayBtn: true
                });
                $('.editFeeConfigEndTime').datetimepicker()
                    .on('changeDate', function (ev) {
                        var value = $(".editFeeConfigEndTime").val();
                        vc.component.editFeeConfigInfo.endTime = value;
                    });
            },
            editFeeConfigValidate: function() {
                return vc.validate.validate({
                    editFeeConfigInfo: vc.component.editFeeConfigInfo
                },
                {
                    'editFeeConfigInfo.feeTypeCd': [{
                        limit: "required",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    {
                        limit: "num",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    ],
                    'editFeeConfigInfo.feeName': [{
                        limit: "required",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    {
                        limit: "maxin",
                        param: "1,100",
                        errInfo: "????????????????????????100???"
                    },
                    ],
                    'editFeeConfigInfo.feeFlag': [{
                        limit: "required",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    {
                        limit: "num",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    ],
                    'editFeeConfigInfo.startTime': [{
                        limit: "required",
                        param: "",
                        errInfo: "??????????????????????????????"
                    },
                    {
                        limit: "dateTime",
                        param: "",
                        errInfo: "?????????????????????????????????????????????"
                    },
                    ],
                    'editFeeConfigInfo.endTime': [{
                        limit: "required",
                        param: "",
                        errInfo: "??????????????????????????????"
                    },
                    {
                        limit: "dateTime",
                        param: "",
                        errInfo: "?????????????????????????????????????????????"
                    },
                    ],
                    'editFeeConfigInfo.computingFormula': [{
                        limit: "required",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    {
                        limit: "num",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    ],
                    'editFeeConfigInfo.squarePrice': [{
                        limit: "required",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    {
                        limit: "money",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    ],
                    'editFeeConfigInfo.additionalAmount': [{
                        limit: "required",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    {
                        limit: "money",
                        param: "",
                        errInfo: "????????????????????????"
                    },
                    ],
                    'editFeeConfigInfo.configId': [{
                        limit: "required",
                        param: "",
                        errInfo: "?????????ID????????????"
                    }]

                });
            },
            editFeeConfig: function() {
            //????????????
                if(vc.component.editFeeConfigValidate.computingFormula == '2002'){
                   vc.component.addFeeConfigInfo.squarePrice = "0.00";
                }
                if (!vc.component.editFeeConfigValidate()) {
                    vc.toast(vc.validate.errInfo);
                    return;
                }

                vc.http.post('editFeeConfig', 'update', JSON.stringify(vc.component.editFeeConfigInfo), {
                    emulateJSON: true
                },
                function(json, res) {
                    //vm.menus = vm.refreshMenuActive(JSON.parse(json),0);
                    if (res.status == 200) {
                        //??????model
                        $('#editFeeConfigModel').modal('hide');
                        vc.emit('feeConfigManage', 'listFeeConfig', {});
                        return;
                    }
                    vc.message(json);
                },
                function(errInfo, error) {
                    console.log('??????????????????');

                    vc.message(errInfo);
                });
            },
            refreshEditFeeConfigInfo: function() {
                var _feeTypeCds = vc.component.editFeeConfigInfo.feeTypeCds;
                var _computingFormulas = vc.component.editFeeConfigInfo.computingFormulas;
                vc.component.editFeeConfigInfo = {
                    configId: '',
                    feeTypeCd: '',
                    feeName: '',
                    feeFlag: '',
                    startTime: '',
                    endTime: '',
                    computingFormula: '',
                    squarePrice: '',
                    additionalAmount: '',
                    isDefault:''
                };
                vc.component.editFeeConfigInfo.feeTypeCds = _feeTypeCds;
                vc.component.editFeeConfigInfo.computingFormulas = _computingFormulas;
            }
        }
    });

})(window.vc, window.vc.component);