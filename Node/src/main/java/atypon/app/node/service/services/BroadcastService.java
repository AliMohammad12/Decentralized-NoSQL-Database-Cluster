package atypon.app.node.service.services;

import atypon.app.node.request.ApiRequest;

public interface BroadcastService {
    <T extends ApiRequest> void ProtectedBroadcast(T request, String endpoint);
    <T extends ApiRequest> void UnprotectedBroadcast(T request, String endpoint);
}
