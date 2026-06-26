package com.tradejob.pro.home.domain

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun getAllClients(): Flow<Result<List<ClientEntity>>>
    suspend fun getClientById(id: Long): Result<ClientEntity?>
    suspend fun insertClient(client: ClientEntity): Result<Long>
    suspend fun updateClient(client: ClientEntity): Result<Unit>
    suspend fun deleteClient(client: ClientEntity): Result<Unit>
    fun searchClients(query: String): Flow<Result<List<ClientEntity>>>
}
