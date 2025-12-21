package com.casarick.app.util;

import com.casarick.app.model.Inventory;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.List;

public class LabelPrinter implements JRDataSource {
    private final List<Inventory> inventoryList;
    private int index = -1;

    public LabelPrinter(List<Inventory> inventoryList) {
        this.inventoryList = inventoryList;
    }

    @Override
    public boolean next() throws JRException {
        index++;
        return (index < inventoryList.size());
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        Inventory currentItem = inventoryList.get(index);
        String fieldName = jrField.getName();

        return switch (fieldName) {
            case "id_category" -> currentItem.getProduct().getCategory().getId();
            case "id_product" -> currentItem.getProduct().getId();
            case "barcode" -> currentItem.getProduct().getBarCodeNumber();
            case "sale_price" -> currentItem.getSalePrice();
            default -> null;
        };
    }
}