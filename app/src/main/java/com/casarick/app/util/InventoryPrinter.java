package com.casarick.app.util;

import com.casarick.app.model.Inventory;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.List;

public class InventoryPrinter implements JRDataSource {
    private final List<Inventory> inventoryList;
    private int index = -1;

    public InventoryPrinter(List<Inventory> inventoryList) {
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
            case "nameBranch" -> SessionManager.getInstance().getCurrentBranch().getName();
            case "id" -> currentInv.getId();
            case "category"  -> currentInv.getProduct().getCategory().getName();
            case "type"   -> currentInv.getProduct().getType().getName();
            case "industry"      -> currentInv.getProduct().getIndustry().getName();
            case "stock"-> currentInv.getStock();
            case "size" -> currentInv.getProduct().getSize();
            case "barCode" -> currentInv.getProduct().getBarCodeNumber();
            case "number"    ->  currentInv.getProduct().getCategory().getId() + "-" + currentInv.getProduct().getId();
            case "cost" -> currentInv.getCostPrice();
            case "sale" -> currentInv.getSalePrice();
            default -> null;
        };
    }
}
