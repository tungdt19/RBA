package com.viettel.vtag.model.entity;

import com.viettel.vtag.model.ILocation;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class FenceCheck {

    private Device device;
    private Fence from;
    private Fence to;
    private ILocation location;
    private String message;
    private Object[] args;
    private boolean change;

    public FenceCheck build() {
        change = true;
        if (from != null && to != null) {
            message = "message.fence.change";
            args = new Object[] {device.name(), from.name(), to.name()};
        }
        if (to != null) {
            message = "message.fence.arrived";
            args = new Object[] {device.name(), to.name()};
        }
        if (from != null) {
            message = "message.fence.gone";
            args = new Object[] {device.name(), from.name()};
        }
        change = false;
        return this;
    }
}
