package com.java110.common.bmo.attendanceClassesTask;

import com.java110.po.attendanceClassesTask.AttendanceClassesTaskPo;
import org.springframework.http.ResponseEntity;
public interface ISaveAttendanceClassesTaskBMO {


    /**
     * 添加考勤任务
     * add by wuxw
     * @param attendanceClassesTaskPo
     * @return
     */
    ResponseEntity<String> save(AttendanceClassesTaskPo attendanceClassesTaskPo);


}
