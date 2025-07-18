package com.warmer.web.service;

import com.warmer.web.request.GraphItem;

import java.io.IOException;

/**
 * (KgGraphNode)表服务接口
 */
public interface KgGraphNodeService {

    void createNode(GraphItem submitItem) throws IOException;

    GraphItem getDomainNode(Integer domainId) throws IOException;
}
