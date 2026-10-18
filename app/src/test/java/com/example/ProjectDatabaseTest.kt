package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.local.ProjectDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ProjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProjectDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var projectDao: ProjectDao
    private lateinit var chatDao: ChatDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        projectDao = db.projectDao()
        chatDao = db.chatDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveProject() = runBlocking {
        val project = ProjectEntity(
            title = "پڕۆژەی وێنەی تاقیکاری",
            category = "IMAGE",
            description = "دەستکاریکراو لە ستۆدیۆی ABDULBASIT SPACE"
        )
        val id = projectDao.insertProject(project)
        assertTrue(id > 0)

        val all = projectDao.getAllProjects().first()
        assertEquals(1, all.size)
        assertEquals("پڕۆژەی وێنەی تاقیکاری", all[0].title)
        assertEquals("IMAGE", all[0].category)
    }

    @Test
    fun insertAndRetrieveChatMessage() = runBlocking {
        val message = ChatMessageEntity(
            conversationId = "default",
            sender = "USER",
            content = "سڵاو، ئەپێک دروست بکە"
        )
        val id = chatDao.insertMessage(message)
        assertTrue(id > 0)

        val messages = chatDao.getMessages("default").first()
        assertEquals(1, messages.size)
        assertEquals("سڵاو، ئەپێک دروست بکە", messages[0].content)
        assertEquals("USER", messages[0].sender)
    }

    @Test
    fun filterProjectsByCategory() = runBlocking {
        projectDao.insertProject(ProjectEntity(title = "وێنە ١", category = "IMAGE"))
        projectDao.insertProject(ProjectEntity(title = "ڤیدیۆ ١", category = "VIDEO"))
        projectDao.insertProject(ProjectEntity(title = "وێنە ٢", category = "IMAGE"))

        val imageProjects = projectDao.getProjectsByCategory("IMAGE").first()
        assertEquals(2, imageProjects.size)

        val videoProjects = projectDao.getProjectsByCategory("VIDEO").first()
        assertEquals(1, videoProjects.size)
    }
}
