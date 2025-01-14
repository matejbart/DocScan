package at.ac.tuwien.caa.docscan.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import at.ac.tuwien.caa.docscan.db.converter.DatabaseTypeConverter
import at.ac.tuwien.caa.docscan.db.dao.DocumentDao
import at.ac.tuwien.caa.docscan.db.dao.ExportFileDao
import at.ac.tuwien.caa.docscan.db.dao.PageDao
import at.ac.tuwien.caa.docscan.db.dao.UserDao
import at.ac.tuwien.caa.docscan.db.model.Document
import at.ac.tuwien.caa.docscan.db.model.ExportFile
import at.ac.tuwien.caa.docscan.db.model.Page
import at.ac.tuwien.caa.docscan.db.model.User

@Database(
    entities = [Document::class, Page::class, User::class, ExportFile::class],
    version = 1
)
@TypeConverters(DatabaseTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun documentDao(): DocumentDao
    abstract fun pageDao(): PageDao
    abstract fun userDao(): UserDao
    abstract fun exportFileDao(): ExportFileDao

    companion object {
        private const val DB_NAME = "docscan.db"

        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
}
