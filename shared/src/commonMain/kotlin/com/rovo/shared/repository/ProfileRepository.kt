package com.rovo.shared.repository

import com.rovo.shared.data.local.dao.AddonDao
import com.rovo.shared.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ProfileRepository(private val dao: AddonDao) {

    fun getProfiles(): Flow<List<ProfileEntity>> = dao.getProfiles()

    suspend fun getProfile(id: Int): ProfileEntity? = dao.getProfileById(id)

    suspend fun saveProfile(profile: ProfileEntity) {
        if (profile.id == 0) {
            dao.insertProfile(profile)
        } else {
            dao.updateProfile(profile)
        }
    }

    suspend fun deleteProfile(id: Int) = dao.deleteProfile(id)

    suspend fun getDefaultProfile(): ProfileEntity {
        val list = dao.getProfiles().firstOrNull() ?: emptyList()
        return list.firstOrNull() 
            ?: ProfileEntity(name = "Default").let { 
                val id = dao.insertProfile(it)
                it.copy(id = id.toInt())
            }
    }
}
