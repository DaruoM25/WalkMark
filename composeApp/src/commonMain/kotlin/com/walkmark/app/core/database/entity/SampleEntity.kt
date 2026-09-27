package com.walkmark.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.walkmark.app.core.model.SampleItem

@Entity(tableName = "samples")
data class SampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long = 0
) {
    fun toDomain(): SampleItem = SampleItem(
        id = id,
        title = title,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(item: SampleItem): SampleEntity = SampleEntity(
            id = item.id,
            title = item.title,
            createdAt = item.createdAt
        )
    }
}
