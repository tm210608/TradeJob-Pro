package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchClientsUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    operator fun invoke(query: String): Flow<Result<List<ClientEntity>>> {
        return clientRepository.searchClients(query)
    }
}
