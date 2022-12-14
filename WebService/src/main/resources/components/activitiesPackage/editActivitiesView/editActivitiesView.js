(function(vc,vm){

    vc.extends({
        data:{
            editActivitiesViewInfo:{
                activitiesId: '',
                title: '',
                typeCd: '',
                headerImg: '',
                context: '',
                startTime: '',
                endTime: ''
            }
        },
         _initMethod:function(){
             vc.component._initEditActivitiesInfo();
         },
         _initEvent:function(){
             vc.on('editActivitiesView','openEditActivitiesModal',function(_params){
                vc.component.refreshEditActivitiesInfo();
                _params.context = filterXSS(_params.context);
                vc.component.editActivitiesViewInfo = _params;
            });
            vc.on('editActivitiesView','activitiesEditActivitiesInfo',function(_params){
                vc.component.refreshEditActivitiesInfo();
                _params.context = filterXSS(_params.context);
                vc.copyObject(_params,vc.component.editActivitiesViewInfo);
                $(".eidtSummernote").summernote('code', vc.component.editActivitiesViewInfo.context);
                var photos = [];
                photos.push(vc.component.editActivitiesViewInfo.headerImg);
                vc.emit('editActivitiesView', 'uploadImage', 'notifyPhotos', photos);
            });

             vc.on("editActivitiesView", "notifyUploadImage", function (_param) {
                 if(!vc.isEmpty(_param) && _param.length >0){
                     vc.component.editActivitiesViewInfo.headerImg = _param[0];
                 }else{
                     vc.component.editActivitiesViewInfo.headerImg = '';
                 }
             });

        },
        methods:{
            editActivitiesValidate:function(){
                return vc.validate.validate({
                    editActivitiesViewInfo: vc.component.editActivitiesViewInfo
                }, {
                    'editActivitiesViewInfo.title': [
                        {
                            limit: "required",
                            param: "",
                            errInfo: "????????????????????????"
                        },
                        {
                            limit: "maxin",
                            param: "1,200",
                            errInfo: "????????????????????????200???"
                        },
                    ],
                    'editActivitiesViewInfo.typeCd': [
                        {
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
                    'editActivitiesViewInfo.headerImg': [
                        {
                            limit: "required",
                            param: "",
                            errInfo: "????????????????????????"
                        }
                    ],
                    'editActivitiesViewInfo.context': [
                        {
                            limit: "required",
                            param: "",
                            errInfo: "????????????????????????"
                        }
                    ],
                    'editActivitiesViewInfo.startTime': [
                        {
                            limit: "required",
                            param: "",
                            errInfo: "????????????????????????"
                        },
                        {
                            limit: "dateTime",
                            param: "",
                            errInfo: "????????????????????????"
                        },
                    ],
                    'editActivitiesViewInfo.endTime': [
                        {
                            limit: "required",
                            param: "",
                            errInfo: "????????????????????????"
                        },
                        {
                            limit: "dateTime",
                            param: "",
                            errInfo: "????????????????????????"
                        },
                    ],
                    'editActivitiesViewInfo.activitiesId': [
                        {
                            limit: "required",
                            param: "",
                            errInfo: "??????ID????????????"
                        }]

                });
             },
            editActivities:function(){
                if(!vc.component.editActivitiesValidate()){
                    vc.toast(vc.validate.errInfo);
                    return ;
                }
                vc.component.editActivitiesViewInfo.communityId = vc.getCurrentCommunity().communityId;

                vc.http.post(
                    'editActivitiesView',
                    'update',
                    JSON.stringify(vc.component.editActivitiesViewInfo),
                    {
                        emulateJSON:true
                     },
                     function(json,res){
                        //vm.menus = vm.refreshMenuActive(JSON.parse(json),0);
                        if(res.status == 200){
                            //??????model
                             vc.emit('activitiesManage','listActivities',{});
                            return ;
                        }
                        vc.message(json);
                     },
                     function(errInfo,error){
                        console.log('??????????????????');

                        vc.message(errInfo);
                     });
            },
            refreshEditActivitiesInfo:function(){
                vc.component.editActivitiesViewInfo= {
                    activitiesId: '',
                    title: '',
                    typeCd: '',
                    headerImg: '',
                    context: '',
                    startTime: '',
                    endTime: '',
                }
            },
            _initEditActivitiesInfo:function(){
                //vc.component.editActivitiesViewInfo.startTime = vc.dateFormat(new Date().getTime());
                 $('.editActivitiesStartTime').datetimepicker({
                    language: 'zh-CN',
                    format: 'yyyy-mm-dd hh:ii:ss',
                    initTime: true,
                    initialDate: new Date(),
                    autoClose: 1,
                    todayBtn: true

                });
                $('.editActivitiesStartTime').datetimepicker()
                    .on('changeDate', function (ev) {
                        var value = $(".editActivitiesStartTime").val();
                        vc.component.editActivitiesViewInfo.startTime = value;
                    });
                $('.editActivitiesEndTime').datetimepicker({
                    language: 'zh-CN',
                    format: 'yyyy-mm-dd hh:ii:ss',
                    initTime: true,
                    initialDate: new Date(),
                    autoClose: 1,
                    todayBtn: true
                });
                $('.editActivitiesEndTime').datetimepicker()
                    .on('changeDate', function (ev) {
                        var value = $(".editActivitiesEndTime").val();
                        vc.component.editActivitiesViewInfo.endTime = value;
                    });
                $('.eidtSummernote').summernote({
                    lang:'zh-CN',
                    height: 300,
                    placeholder:'??????????????????????????????',
                    callbacks : {
                         onImageUpload: function(files, editor, $editable) {
                             vc.component.sendEditFile(files);
                         },
                         onChange:function(contents,$editable){
                            vc.component.editActivitiesViewInfo.context = contents;
                         }
                    }
                });

            },
            sendEditFile:function(files){
                console.log('????????????');
            },
            closeEditActivitiesInfo:function(){
                 vc.emit('activitiesManage','listActivities',{});

            },
        }
    });

})(window.vc,window.vc.component);
