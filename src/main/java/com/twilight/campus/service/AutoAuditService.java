package com.twilight.campus.service;

import com.twilight.campus.dto.AutoAuditConfigDTO;
import com.twilight.campus.vo.AutoAuditConfigVO;

public interface AutoAuditService {
    AutoAuditConfigVO getConfig();

    void updateConfig(AutoAuditConfigDTO dto);

    boolean isEnabled();

    AuditDecision review(String title, String content);

    class AuditDecision {
        private final boolean pass;
        private final String reason;

        public AuditDecision(boolean pass, String reason) {
            this.pass = pass;
            this.reason = reason;
        }

        public boolean isPass() {
            return pass;
        }

        public String getReason() {
            return reason;
        }
    }
}
