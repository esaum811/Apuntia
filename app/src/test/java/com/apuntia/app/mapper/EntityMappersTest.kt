package com.apuntia.app.mapper

import com.apuntia.app.data.local.database.DefaultCategories
import com.apuntia.app.data.mapper.toDomain
import com.apuntia.app.data.mapper.toEntity
import com.apuntia.app.domain.model.CategoryShape
import com.apuntia.app.domain.model.Entry
import com.apuntia.app.domain.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntityMappersTest {

    @Test
    fun categoryRoundTripKeepsAllFields() {
        val entity = DefaultCategories.all.first()
        val domain = entity.toDomain()
        assertEquals(CategoryShape.CIRCLE, domain.shape)
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun unknownShapeFallsBackToCircle() {
        assertEquals(CategoryShape.CIRCLE, CategoryShape.fromName("OTRA"))
    }

    @Test
    fun entryRoundTripKeepsFieldsAndNeverWritesEncryptedBody() {
        val entry = Entry("1", "school", EntryType.TASK, "Ensayo", "Texto", false, 99L, false, true, 1L, 2L, null)
        val entity = entry.toEntity()
        assertNull(entity.bodyEncrypted)
        assertEquals(entry, entity.toDomain())
    }
}
