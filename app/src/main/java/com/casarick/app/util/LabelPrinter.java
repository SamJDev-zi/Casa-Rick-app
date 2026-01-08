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
        return ++index < inventoryList.size();
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        Inventory currentInv = inventoryList.get(index);
        return switch (jrField.getName()) {
            case "inventory_id" -> currentInv.getId();
            case "categoty_id"  -> currentInv.getProduct().getCategory().getId();
            case "product_id"   -> currentInv.getProduct().getId();
            case "barCode"      -> currentInv.getProduct().getBarCodeNumber();
            case "category_name"-> currentInv.getProduct().getCategory().getName();
            case "type_name"    -> currentInv.getProduct().getName();
            case "price"        -> currentInv.getSalePrice();
            default -> null;
        };
    }
}