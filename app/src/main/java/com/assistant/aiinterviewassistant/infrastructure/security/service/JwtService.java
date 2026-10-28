package com.assistant.aiinterviewassistant.infrastructure.security.service;

public interface JwtService {
        /**
         * 生成token（默认时间）
         * @param userId 数据库唯一主键
         * @param sessionId 唯一会话id，目前使用uuid，该值会存进redis
         * @return token
         */
        public String generateToken(Long userId,String sessionId);
        /**
         * 生成token（自己设定时间）
         * @param userId 数据库唯一主键
         * @param sessionId 唯一会话id，目前使用uuid，该值会存进redis
         * @param validitySeconds 有效期,单位为秒
         * @return token
         */
        public String generateToken(Long userId,String sessionId,long validitySeconds);
}
