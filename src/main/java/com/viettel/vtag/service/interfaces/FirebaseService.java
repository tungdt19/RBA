package com.viettel.vtag.service.interfaces;

import java.util.List;
import java.util.Map;

public interface FirebaseService {

    void message(List<String> topicTokens, Map<String, String> data);
}
