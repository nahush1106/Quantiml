package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  fun getUserById(userId: String): Flow<UserEntity?>

  @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
  suspend fun getUserByEmail(email: String): UserEntity?

  @Query("SELECT * FROM users WHERE mobileNumber = :mobile LIMIT 1")
  suspend fun getUserByMobile(mobile: String): UserEntity?

  @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:identifier) OR mobileNumber = :identifier LIMIT 1")
  suspend fun getUserByIdentifier(identifier: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Update
  suspend fun updateUser(user: UserEntity)

  @Query("SELECT COUNT(*) FROM users")
  suspend fun getUserCount(): Int

  @Query("SELECT * FROM users")
  suspend fun getAllUsers(): List<UserEntity>
}
