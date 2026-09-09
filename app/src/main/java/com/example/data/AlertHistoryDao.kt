package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertHistoryDao {

    @Query("SELECT * FROM alert_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllAlertsFlow(): Flow<List<AlertNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertNotificationEntity): Long

    @Query("DELETE FROM alert_history")
    suspend fun clearHistory()
}
