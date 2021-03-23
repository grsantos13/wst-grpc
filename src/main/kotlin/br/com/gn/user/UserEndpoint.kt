package br.com.gn.user

import br.com.gn.DeleteUserRequest
import br.com.gn.NewUserRequest
import br.com.gn.ReadUserRequest
import br.com.gn.UpdateUserRequest
import br.com.gn.UserResponse
import br.com.gn.UserServiceGrpc
import br.com.gn.UsersResponse
import br.com.gn.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class UserEndpoint(
    private val service: UserService
) : UserServiceGrpc.UserServiceImplBase() {
    override fun create(request: NewUserRequest, responseObserver: StreamObserver<UserResponse>) {
        val user = service.create(request.toRequestModel())
        val response = grpcUserResponse(user)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun read(request: ReadUserRequest, responseObserver: StreamObserver<UsersResponse>) {
        val responseList = service.read(request)
            .map { grpcUserResponse(it) }

        val response = UsersResponse.newBuilder()
            .addAllUsers(responseList)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun update(request: UpdateUserRequest, responseObserver: StreamObserver<UserResponse>) {
        val user = service.update(request.toRequestModel(), request.id)
        val response = grpcUserResponse(user)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    override fun delete(request: DeleteUserRequest, responseObserver: StreamObserver<UserResponse>) {
        val user: User = service.delete(request.id)
        val response = grpcUserResponse(user)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }

    private fun grpcUserResponse(user: User) = UserResponse.newBuilder()
        .setEmail(user.email)
        .setId(user.id.toString())
        .setName(user.name)
        .build()
}