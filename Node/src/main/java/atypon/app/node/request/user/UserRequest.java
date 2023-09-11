package atypon.app.node.request.user;

import atypon.app.node.model.User;
import atypon.app.node.request.ApiRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class UserRequest {
    private User user;
}
