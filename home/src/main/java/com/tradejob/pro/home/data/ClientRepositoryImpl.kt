package com.tradejob.pro.home.data

import com.tradejob.pro.common.usecase.Result
import com.tradejob.pro.database.data.dao.ClientDao
import com.tradejob.pro.database.data.entity.ClientEntity
import com.tradejob.pro.home.domain.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val clientDao: ClientDao
) : ClientRepository {

    override fun getAllClients(): Flow<Result<List<ClientEntity>>> = flow {
        emit(Result.Loading)
        try {
            clientDao.getAllClients().collect { clients ->
                emit(Result.Success(clients))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    override suspend fun getClientById(id: Long): Result<ClientEntity?> {
        return try {
            Result.Success(clientDao.getClientById(id))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun insertClient(client: ClientEntity): Result<Long> {
        return try {
            val id = clientDao.insert(client)
            Result.Success(id)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateClient(client: ClientEntity): Result<Unit> {
        return try {
            clientDao.update(client)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteClient(client: ClientEntity): Result<Unit> {
        return try {
            clientDao.delete(client)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun searchClients(query: String): Flow<Result<List<ClientEntity>>> = flow {
        emit(Result.Loading)
        try {
            clientDao.searchClients(query).collect { clients ->
                emit(Result.Success(clients))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
