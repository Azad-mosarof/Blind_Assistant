package com.example.multiTaskingApp.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.multiTaskingApp.data.entities.Analytics;
import com.example.multiTaskingApp.data.entities.TaskAttempt;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ReadWriteExternalStorage {
    private final static String ROOT_DIR = "Blind Assistant";

    // For analytics
    private final static String ANALYTICS_DATABASE = "Analytics Database";
    private final static String ANALYTICS_DATABASE_FLOW_ID_COL = "Flow ID";
    private final static String ANALYTICS_DATABASE_TASK_ID_COL = "Task ID";
    private final static String ANALYTICS_DATABASE_TASK_NO_COL = "Task Number";
    private final static String ANALYTICS_DATABASE_ATTEMPTS_COL = "Attempts";
    private final static String ANALYTICS_DATABASE_TASK_START_DATETIME_COL = "Task Start Datetime";
    private final static String ANALYTICS_DATABASE_TASK_END_DATETIME_COL = "Task End Datetime";
    private final static String ANALYTICS_DATABASE_TASK_DELTA_DATETIME_COL = "Task Delta Datetime";
    private final static String ANALYTICS_DATABASE_DETECTED_CURRENCY_COL = "Detected Currency";

    // For task attempts
    private final static String TASK_ATTEMPT_DATABASE = "Task Attempt Database";
    private final static String TASK_ATTEMPT_DATABASE_TASK_ID_COL = "Task ID";
    private final static String TASK_ATTEMPT_DATABASE_ATTEMPT_NO_COL = "Attempt Number";
    private final static String TASK_ATTEMPT_DATABASE_ATTEMPT_START_DATETIME_COL = "Attempt Start Datetime";
    private final static String TASK_ATTEMPT_DATABASE_ATTEMPT_END_DATETIME_COL = "Attempt End Datetime";
    private final static String TASK_ATTEMPT_DATABASE_ATTEMPT_DELTA_DATETIME_COL = "Attempt Delta Datetime";
    private final static String TASK_ATTEMPT_DATABASE_USER_RESPONSE_COL = "User Response";

    private final Context context;
    private final String folder_name;
    private final String file_name;

    public ReadWriteExternalStorage(String folder_name, String file_name, Context context) {
        this.folder_name = folder_name;
        this.file_name = file_name;
        this.context = context;
    }

    public File analyticsExportToExcel(ArrayList<Analytics> analyticsArrayList, ArrayList<TaskAttempt> taskAttemptArrayList) {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();

        // Adding analytics sheet
        HSSFSheet hssfSheetAnalytics = hssfWorkbook.createSheet(ANALYTICS_DATABASE);
        HSSFRow hssfRowAnalytics = hssfSheetAnalytics.createRow(0);

        hssfRowAnalytics.createCell(0).setCellValue(ANALYTICS_DATABASE_FLOW_ID_COL);
        hssfRowAnalytics.createCell(1).setCellValue(ANALYTICS_DATABASE_TASK_ID_COL);
        hssfRowAnalytics.createCell(2).setCellValue(ANALYTICS_DATABASE_TASK_NO_COL);
        hssfRowAnalytics.createCell(3).setCellValue(ANALYTICS_DATABASE_ATTEMPTS_COL);
        hssfRowAnalytics.createCell(4).setCellValue(ANALYTICS_DATABASE_TASK_START_DATETIME_COL);
        hssfRowAnalytics.createCell(5).setCellValue(ANALYTICS_DATABASE_TASK_END_DATETIME_COL);
        hssfRowAnalytics.createCell(6).setCellValue(ANALYTICS_DATABASE_TASK_DELTA_DATETIME_COL);
        hssfRowAnalytics.createCell(7).setCellValue(ANALYTICS_DATABASE_DETECTED_CURRENCY_COL);

        int row_num = 1;
        for (Analytics analytics : analyticsArrayList) {
            String analyticsFlowId = analytics.getFlowId();
            String analyticsTaskId = analytics.getTaskId();
            String analyticsTaskNo = String.valueOf(analytics.getTaskNo());
            String analyticsAttempts = String.valueOf(analytics.getAttempts());
            String analyticsTaskStartDateTime = String.valueOf(analytics.getTaskStartDateTime().getTime());
            String analyticsTaskEndDateTime = String.valueOf(analytics.getTaskEndDateTime().getTime());
            String analyticsTaskDeltaDateTime = String.valueOf(analytics.getTaskDeltaDateTime());
            String analyticsDetectedCurrency = analytics.getDetectedCurrency();

            HSSFRow curr_row = hssfSheetAnalytics.createRow(row_num);

            curr_row.createCell(0).setCellValue(analyticsFlowId);
            curr_row.createCell(1).setCellValue(analyticsTaskId);
            curr_row.createCell(2).setCellValue(analyticsTaskNo);
            curr_row.createCell(3).setCellValue(analyticsAttempts);
            curr_row.createCell(4).setCellValue(analyticsTaskStartDateTime);
            curr_row.createCell(5).setCellValue(analyticsTaskEndDateTime);
            curr_row.createCell(6).setCellValue(analyticsTaskDeltaDateTime);
            curr_row.createCell(7).setCellValue(analyticsDetectedCurrency);
            ++row_num;
        }

        // Adding task attempts sheet
        HSSFSheet hssfSheetTaskAttempt = hssfWorkbook.createSheet(TASK_ATTEMPT_DATABASE);
        HSSFRow hssfRowTaskAttempt = hssfSheetTaskAttempt.createRow(0);

        hssfRowTaskAttempt.createCell(0).setCellValue(TASK_ATTEMPT_DATABASE_TASK_ID_COL);
        hssfRowTaskAttempt.createCell(1).setCellValue(TASK_ATTEMPT_DATABASE_ATTEMPT_NO_COL);
        hssfRowTaskAttempt.createCell(3).setCellValue(TASK_ATTEMPT_DATABASE_ATTEMPT_START_DATETIME_COL);
        hssfRowTaskAttempt.createCell(4).setCellValue(TASK_ATTEMPT_DATABASE_ATTEMPT_END_DATETIME_COL);
        hssfRowTaskAttempt.createCell(5).setCellValue(TASK_ATTEMPT_DATABASE_ATTEMPT_DELTA_DATETIME_COL);
        hssfRowTaskAttempt.createCell(6).setCellValue(TASK_ATTEMPT_DATABASE_USER_RESPONSE_COL);

        row_num = 1;
        for (TaskAttempt taskAttempt : taskAttemptArrayList) {
            String taskAttemptTaskId = taskAttempt.getTaskId();
            String taskAttemptAttemptNo = String.valueOf(taskAttempt.getAttemptNo());
            String taskAttemptStartDateTime = String.valueOf(taskAttempt.getAttemptStartDateTime().getTime());
            String taskAttemptEndDateTime = String.valueOf(taskAttempt.getAttemptEndDateTime().getTime());
            String taskAttemptDeltaDateTime = String.valueOf(taskAttempt.getAttemptDeltaDateTime());
            String taskAttemptUserResponse = taskAttempt.getUserResponse();

            HSSFRow curr_row = hssfSheetTaskAttempt.createRow(row_num);

            curr_row.createCell(0).setCellValue(taskAttemptTaskId);
            curr_row.createCell(1).setCellValue(taskAttemptAttemptNo);
            curr_row.createCell(2).setCellValue(taskAttemptStartDateTime);
            curr_row.createCell(3).setCellValue(taskAttemptEndDateTime);
            curr_row.createCell(4).setCellValue(taskAttemptDeltaDateTime);
            curr_row.createCell(5).setCellValue(taskAttemptUserResponse);
            ++row_num;
        }

        File filePathFinal = null;

        try {
            File baseDir = new File(Environment.getExternalStorageDirectory(), ROOT_DIR);
            if (!baseDir.exists()) {
                baseDir.mkdir();
            }

            File currDir = new File(Environment.getExternalStorageDirectory() + "/" + ROOT_DIR, folder_name);
            if (!currDir.exists()) currDir.mkdir();

            File filePath = new File(currDir.getAbsoluteFile(), file_name + ".xls");

            // from here
            if (filePath.exists()) {
                filePath.delete();
            }
            filePath.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            hssfWorkbook.write(fileOutputStream);

            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
            filePathFinal = filePath;
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Help", "Failed to create Excel Sheet");
            Log.e("Help", e.toString());
            Toast.makeText(context, "Failed to create Excel Sheet : " + e, Toast.LENGTH_SHORT).show();
        }
        return filePathFinal;
    }
}
