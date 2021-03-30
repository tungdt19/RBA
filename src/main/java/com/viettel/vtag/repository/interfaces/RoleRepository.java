package com.viettel.vtag.repository.interfaces;

import com.viettel.vtag.model.entity.AppRole;

import java.util.List;

public interface RoleRepository {
    List<AppRole> getRoles(int userId);

    List<AppRole> getRoles(String token);
}
