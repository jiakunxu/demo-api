package com.example.demo.framework.util;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    public static void download(InputStream in, OutputStream out) throws IOException {
        Workbook workbook = null;

        try {
            workbook = WorkbookFactory.create(in);

            workbook.write(out);
        } finally {
            IOUtils.closeQuietly(workbook);
        }
    }

    public static <T> List<T> read(InputStream in, Class<T> clazz,
                                   List<String> props) throws IOException, InstantiationException,
                                                       IllegalAccessException,
                                                       InvocationTargetException {
        if (props == null) {
            return null;
        }

        Workbook workbook = null;

        try {
            workbook = WorkbookFactory.create(in);
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getLastRowNum() == 0) {
                return null;
            }

            List<T> list = new ArrayList<T>();

            for (int rownum = 2; rownum <= sheet.getLastRowNum(); rownum++) {
                Row row = sheet.getRow(rownum);
                int column = 0;

                if (row.getCell(0) == null || row.getCell(0).getCellType() == CellType.BLANK) {
                    continue;
                }

                T object = clazz.newInstance();

                for (String prop : props) {
                    Object obj = object;

                    int length = prop.split("[.]").length;

                    for (int i = 0; i < length; i++) {
                        String p = prop.split("[.]")[i];

                        if (i == length - 1) {
                            Method setMethod = findSetMethod(obj, p);
                            if (setMethod != null) {
                                setMethod.invoke(obj,
                                    getCell(row, column++, setMethod.getParameterTypes()[0]));
                            }
                            break;
                        }

                        Method getMethod = findGetMethod(obj, p);
                        if (getMethod == null) {
                            break;
                        }
                        Object sub = getMethod.invoke(obj);
                        if (sub != null) {
                            obj = sub;
                            continue;
                        }
                        Method setMethod = findSetMethod(obj, p);
                        if (setMethod == null) {
                            break;
                        }
                        sub = setMethod.getParameterTypes()[0].newInstance();
                        setMethod.invoke(obj, sub);
                        obj = sub;
                    }
                }

                list.add(object);
            }

            return list;
        } finally {
            IOUtils.closeQuietly(workbook);
        }
    }

    public static void write(InputStream in, OutputStream out, List<?> dataList,
                             List<String> props) throws IOException, InvocationTargetException,
                                                 IllegalAccessException {
        if (props == null) {
            return;
        }

        SXSSFWorkbook workbook = null;

        try {
            workbook = new SXSSFWorkbook(new XSSFWorkbook(in));

            Sheet sheet = workbook.getSheetAt(0);
            int rownum = 2;

            if (CollectionUtils.isNotEmpty(dataList)) {
                XSSFSheet xssfSheet = workbook.getXSSFWorkbook().getSheetAt(0);

                CellStyle rowStyle = getRowStyle(xssfSheet, rownum);
                CellType[] cellType = getCellType(xssfSheet, rownum, props.size());
                CellStyle[] cellStyle = getCellStyle(xssfSheet, rownum, props.size());

                for (Object object : dataList) {
                    if (rownum == 2) {
                        setRow(xssfSheet, rownum++, object, props, rowStyle, cellType, cellStyle);
                        continue;
                    }

                    setRow(sheet, rownum++, object, props, rowStyle, cellType, cellStyle);
                }
            }

            workbook.write(out);
        } finally {
            IOUtils.closeQuietly(workbook);
        }
    }

    public static void write(InputStream in, OutputStream out, Object data,
                             Map<String, List<String>> props) throws IOException,
                                                              InvocationTargetException,
                                                              IllegalAccessException {
        if (props == null) {
            return;
        }

        Workbook workbook = null;

        try {
            workbook = WorkbookFactory.create(in);

            int index = 0;

            if (data != null) {
                for (Map.Entry<String, List<String>> map : props.entrySet()) {
                    Sheet sheet = workbook.getSheetAt(index++);
                    int rownum = 2;

                    Method getMethod = findGetMethod(data, map.getKey());
                    if (getMethod == null) {
                        continue;
                    }
                    Object obj = getMethod.invoke(data);
                    if (obj == null) {
                        continue;
                    }

                    if (obj instanceof List) {
                        for (Object object : (List) obj) {
                            setRow(sheet, rownum++, object, map.getValue(), null, null, null);
                        }
                    } else {
                        setRow(sheet, rownum, obj, map.getValue(), null, null, null);
                    }
                }
            }

            workbook.write(out);
        } finally {
            IOUtils.closeQuietly(workbook);
        }
    }

    private static Method findGetMethod(Object object, String name) {
        return BeanUtils.findMethod(object.getClass(), "get" + StringUtils.capitalize(name));
    }

    private static Method findSetMethod(Object object, String name) {
        Method[] methods = object.getClass().getMethods();

        for (Method method : methods) {
            if (method.getName().equals("set" + StringUtils.capitalize(name))
                && method.getParameterTypes().length == 1) {
                return method;
            }
        }

        return null;
    }

    private static void setRow(Sheet sheet, int rownum, Object object, List<String> props,
                               CellStyle rowStyle, CellType[] cellType,
                               CellStyle[] cellStyle) throws InvocationTargetException,
                                                      IllegalAccessException {
        Row row = sheet.getRow(rownum);

        if (row == null) {
            row = sheet.createRow(rownum);
            row.setRowStyle(rowStyle);
        }

        int column = 0;

        for (String prop : props) {
            Object obj = object;

            for (String p : prop.split("[.]")) {
                Method getMethod = findGetMethod(obj, p);
                if (getMethod == null) {
                    obj = null;
                    break;
                }
                obj = getMethod.invoke(obj);
                if (obj == null) {
                    break;
                }
            }

            setCell(sheet, rownum, column, obj, cellType == null ? null : cellType[column],
                cellStyle == null ? null : cellStyle[column]);

            column++;
        }
    }

    private static <T> T getCell(Row row, int cellnum, Class<T> clazz) {
        Cell cell = row.getCell(cellnum);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC || cell.getCellType() == CellType.FORMULA) {
            if (DateUtil.isCellDateFormatted(cell)) {
                if (clazz == Date.class) {
                    return JSON.parseObject(
                        JSON.toJSONString(DateUtil.getJavaDate(cell.getNumericCellValue())), clazz);
                }
            } else {
                return JSON.parseObject(
                    JSON.toJSONString(new BigDecimal(cell.getNumericCellValue())), clazz);
            }
        }

        if (cell.getCellType() == CellType.STRING) {
            return JSON.parseObject(JSON.toJSONString(cell.getStringCellValue()), clazz);
        }

        if (cell.getCellType() == CellType.BOOLEAN) {
            return JSON.parseObject(JSON.toJSONString(cell.getBooleanCellValue()), clazz);
        }

        if (cell.getCellType() == CellType.ERROR) {
            return JSON.parseObject(JSON.toJSONString(cell.getErrorCellValue()), clazz);
        }

        return null;
    }

    private static void setCell(Sheet sheet, int rownum, int cellnum, Object value,
                                CellType cellType, CellStyle cellStyle) {
        Cell cell = sheet.getRow(rownum).getCell(cellnum);

        if (cell == null) {
            cell = sheet.getRow(rownum).createCell(cellnum, cellType);
            cell.setCellStyle(cellStyle);
        }

        if (value == null) {
            return;
        }

        if (value instanceof Double) {
            cell.setCellValue((Double) value);
            return;
        }

        if (value instanceof Date) {
            cell.setCellValue((Date) value);
            return;
        }

        if (value instanceof String) {
            cell.setCellValue((String) value);
            return;
        }

        if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
            return;
        }

        cell.setCellValue(JSON.toJSONString(value));
    }

    private static CellStyle getRowStyle(Sheet sheet, int rownum) {
        return sheet.getRow(rownum).getRowStyle();
    }

    private static CellType[] getCellType(Sheet sheet, int rownum, int cellnum) {
        Row row = sheet.getRow(rownum);
        List<CellType> list = new ArrayList<>();

        for (int i = 0; i < cellnum; i++) {
            list.add(row.getCell(i).getCellType());
        }

        return list.toArray(new CellType[0]);
    }

    private static CellStyle[] getCellStyle(Sheet sheet, int rownum, int cellnum) {
        Row row = sheet.getRow(rownum);
        List<CellStyle> list = new ArrayList<>();

        for (int i = 0; i < cellnum; i++) {
            list.add(row.getCell(i).getCellStyle());
        }

        return list.toArray(new CellStyle[0]);
    }

}
