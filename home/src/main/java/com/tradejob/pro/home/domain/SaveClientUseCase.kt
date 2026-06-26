package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SaveClientUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(client: ClientEntity, isNew: Boolean = true): Flow<Result<Long>> = flow {
        emit(Result.Loading)
        if (isNew) {
            emit(clientRepository.insertClient(client))
        } else {
            when (val result = clientRepository.updateClient(client)) {
                is Result.Success -> emit(Result.Success(client.id))
                is Result.Error -> emit(Result.Error(result.exception))
                else -> {}
            }
        }
    }
}
