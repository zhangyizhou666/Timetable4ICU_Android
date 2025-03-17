package com.zhangyizhou666.timetable4icu_ver2.data.model

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

// Main cell model that contains schedule information
class CellModel : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var tableTitle: String = ""
    var year: String = ""
    var term: String = ""
    
    var courseTitle: CourseTitleModel? = null
    var instructor: InstructorModel? = null
    var schedule: ScheduleModel? = null
    var courseno: CoursenoModel? = null
    var room: RoomModel? = null
    var mode: ModeModel? = null
    var color: ColorModel? = null
    
    var tasks: RealmList<Task> = realmListOf()
}

// Model for course titles in each time slot
class CourseTitleModel : RealmObject {
    var id: String = ""
    
    // Monday slots
    var M1: String = ""
    var M2: String = ""
    var M3: String = ""
    var ML: String = "" // Lunch time
    var M4: String = ""
    var M5: String = ""
    var M6: String = ""
    var M7: String = ""
    var M8: String = ""
    
    // Tuesday slots
    var TU1: String = ""
    var TU2: String = ""
    var TU3: String = ""
    var TUL: String = "" // Lunch time
    var TU4: String = ""
    var TU5: String = ""
    var TU6: String = ""
    var TU7: String = ""
    var TU8: String = ""
    
    // Wednesday slots
    var W1: String = ""
    var W2: String = ""
    var W3: String = ""
    var WL: String = "" // Lunch time
    var W4: String = ""
    var W5: String = ""
    var W6: String = ""
    var W7: String = ""
    var W8: String = ""
    
    // Thursday slots
    var TH1: String = ""
    var TH2: String = ""
    var TH3: String = ""
    var THL: String = "" // Lunch time
    var TH4: String = ""
    var TH5: String = ""
    var TH6: String = ""
    var TH7: String = ""
    var TH8: String = ""
    
    // Friday slots
    var F1: String = ""
    var F2: String = ""
    var F3: String = ""
    var FL: String = "" // Lunch time
    var F4: String = ""
    var F5: String = ""
    var F6: String = ""
    var F7: String = ""
    var F8: String = ""
    
    // Saturday slots
    var SA1: String = ""
    var SA2: String = ""
    var SA3: String = ""
    var SAL: String = "" // Lunch time
    var SA4: String = ""
    var SA5: String = ""
    var SA6: String = ""
    var SA7: String = ""
    var SA8: String = ""
}

// Model for instructor information in each time slot
class InstructorModel : RealmObject {
    var id: String = ""
    
    // Monday slots
    var M1: String = ""
    var M2: String = ""
    var M3: String = ""
    var ML: String = ""
    var M4: String = ""
    var M5: String = ""
    var M6: String = ""
    var M7: String = ""
    var M8: String = ""
    
    // Tuesday slots
    var TU1: String = ""
    var TU2: String = ""
    var TU3: String = ""
    var TUL: String = ""
    var TU4: String = ""
    var TU5: String = ""
    var TU6: String = ""
    var TU7: String = ""
    var TU8: String = ""
    
    // Wednesday slots
    var W1: String = ""
    var W2: String = ""
    var W3: String = ""
    var WL: String = ""
    var W4: String = ""
    var W5: String = ""
    var W6: String = ""
    var W7: String = ""
    var W8: String = ""
    
    // Thursday slots
    var TH1: String = ""
    var TH2: String = ""
    var TH3: String = ""
    var THL: String = ""
    var TH4: String = ""
    var TH5: String = ""
    var TH6: String = ""
    var TH7: String = ""
    var TH8: String = ""
    
    // Friday slots
    var F1: String = ""
    var F2: String = ""
    var F3: String = ""
    var FL: String = ""
    var F4: String = ""
    var F5: String = ""
    var F6: String = ""
    var F7: String = ""
    var F8: String = ""
    
    // Saturday slots
    var SA1: String = ""
    var SA2: String = ""
    var SA3: String = ""
    var SAL: String = ""
    var SA4: String = ""
    var SA5: String = ""
    var SA6: String = ""
    var SA7: String = ""
    var SA8: String = ""
}

// Model for schedule information in each time slot
class ScheduleModel : RealmObject {
    var id: String = ""
    
    // Monday slots
    var M1: String = ""
    var M2: String = ""
    var M3: String = ""
    var ML: String = ""
    var M4: String = ""
    var M5: String = ""
    var M6: String = ""
    var M7: String = ""
    var M8: String = ""
    
    // Tuesday slots
    var TU1: String = ""
    var TU2: String = ""
    var TU3: String = ""
    var TUL: String = ""
    var TU4: String = ""
    var TU5: String = ""
    var TU6: String = ""
    var TU7: String = ""
    var TU8: String = ""
    
    // Wednesday slots
    var W1: String = ""
    var W2: String = ""
    var W3: String = ""
    var WL: String = ""
    var W4: String = ""
    var W5: String = ""
    var W6: String = ""
    var W7: String = ""
    var W8: String = ""
    
    // Thursday slots
    var TH1: String = ""
    var TH2: String = ""
    var TH3: String = ""
    var THL: String = ""
    var TH4: String = ""
    var TH5: String = ""
    var TH6: String = ""
    var TH7: String = ""
    var TH8: String = ""
    
    // Friday slots
    var F1: String = ""
    var F2: String = ""
    var F3: String = ""
    var FL: String = ""
    var F4: String = ""
    var F5: String = ""
    var F6: String = ""
    var F7: String = ""
    var F8: String = ""
    
    // Saturday slots
    var SA1: String = ""
    var SA2: String = ""
    var SA3: String = ""
    var SAL: String = ""
    var SA4: String = ""
    var SA5: String = ""
    var SA6: String = ""
    var SA7: String = ""
    var SA8: String = ""
}

// Model for course numbers in each time slot
class CoursenoModel : RealmObject {
    var id: String = ""
    
    // Monday slots
    var M1: String = ""
    var M2: String = ""
    var M3: String = ""
    var ML: String = ""
    var M4: String = ""
    var M5: String = ""
    var M6: String = ""
    var M7: String = ""
    var M8: String = ""
    
    // Tuesday slots
    var TU1: String = ""
    var TU2: String = ""
    var TU3: String = ""
    var TUL: String = ""
    var TU4: String = ""
    var TU5: String = ""
    var TU6: String = ""
    var TU7: String = ""
    var TU8: String = ""
    
    // Wednesday slots
    var W1: String = ""
    var W2: String = ""
    var W3: String = ""
    var WL: String = ""
    var W4: String = ""
    var W5: String = ""
    var W6: String = ""
    var W7: String = ""
    var W8: String = ""
    
    // Thursday slots
    var TH1: String = ""
    var TH2: String = ""
    var TH3: String = ""
    var THL: String = ""
    var TH4: String = ""
    var TH5: String = ""
    var TH6: String = ""
    var TH7: String = ""
    var TH8: String = ""
    
    // Friday slots
    var F1: String = ""
    var F2: String = ""
    var F3: String = ""
    var FL: String = ""
    var F4: String = ""
    var F5: String = ""
    var F6: String = ""
    var F7: String = ""
    var F8: String = ""
    
    // Saturday slots
    var SA1: String = ""
    var SA2: String = ""
    var SA3: String = ""
    var SAL: String = ""
    var SA4: String = ""
    var SA5: String = ""
    var SA6: String = ""
    var SA7: String = ""
    var SA8: String = ""
}

// Model for room information in each time slot
class RoomModel : RealmObject {
    var id: String = ""
    
    // Monday slots
    var M1: String = ""
    var M2: String = ""
    var M3: String = ""
    var ML: String = ""
    var M4: String = ""
    var M5: String = ""
    var M6: String = ""
    var M7: String = ""
    var M8: String = ""
    
    // Tuesday slots
    var TU1: String = ""
    var TU2: String = ""
    var TU3: String = ""
    var TUL: String = ""
    var TU4: String = ""
    var TU5: String = ""
    var TU6: String = ""
    var TU7: String = ""
    var TU8: String = ""
    
    // Wednesday slots
    var W1: String = ""
    var W2: String = ""
    var W3: String = ""
    var WL: String = ""
    var W4: String = ""
    var W5: String = ""
    var W6: String = ""
    var W7: String = ""
    var W8: String = ""
    
    // Thursday slots
    var TH1: String = ""
    var TH2: String = ""
    var TH3: String = ""
    var THL: String = ""
    var TH4: String = ""
    var TH5: String = ""
    var TH6: String = ""
    var TH7: String = ""
    var TH8: String = ""
    
    // Friday slots
    var F1: String = ""
    var F2: String = ""
    var F3: String = ""
    var FL: String = ""
    var F4: String = ""
    var F5: String = ""
    var F6: String = ""
    var F7: String = ""
    var F8: String = ""
    
    // Saturday slots
    var SA1: String = ""
    var SA2: String = ""
    var SA3: String = ""
    var SAL: String = ""
    var SA4: String = ""
    var SA5: String = ""
    var SA6: String = ""
    var SA7: String = ""
    var SA8: String = ""
}

// Model for teaching mode information in each time slot
class ModeModel : RealmObject {
    var id: String = ""
    
    // Monday slots
    var M1: String = ""
    var M2: String = ""
    var M3: String = ""
    var ML: String = ""
    var M4: String = ""
    var M5: String = ""
    var M6: String = ""
    var M7: String = ""
    var M8: String = ""
    
    // Tuesday slots
    var TU1: String = ""
    var TU2: String = ""
    var TU3: String = ""
    var TUL: String = ""
    var TU4: String = ""
    var TU5: String = ""
    var TU6: String = ""
    var TU7: String = ""
    var TU8: String = ""
    
    // Wednesday slots
    var W1: String = ""
    var W2: String = ""
    var W3: String = ""
    var WL: String = ""
    var W4: String = ""
    var W5: String = ""
    var W6: String = ""
    var W7: String = ""
    var W8: String = ""
    
    // Thursday slots
    var TH1: String = ""
    var TH2: String = ""
    var TH3: String = ""
    var THL: String = ""
    var TH4: String = ""
    var TH5: String = ""
    var TH6: String = ""
    var TH7: String = ""
    var TH8: String = ""
    
    // Friday slots
    var F1: String = ""
    var F2: String = ""
    var F3: String = ""
    var FL: String = ""
    var F4: String = ""
    var F5: String = ""
    var F6: String = ""
    var F7: String = ""
    var F8: String = ""
    
    // Saturday slots
    var SA1: String = ""
    var SA2: String = ""
    var SA3: String = ""
    var SAL: String = ""
    var SA4: String = ""
    var SA5: String = ""
    var SA6: String = ""
    var SA7: String = ""
    var SA8: String = ""
}

// Model for color information in each time slot
class ColorModel : RealmObject {
    var id: String = ""
    
    // Monday slots
    var M1: String = ""
    var M2: String = ""
    var M3: String = ""
    var ML: String = ""
    var M4: String = ""
    var M5: String = ""
    var M6: String = ""
    var M7: String = ""
    var M8: String = ""
    
    // Tuesday slots
    var TU1: String = ""
    var TU2: String = ""
    var TU3: String = ""
    var TUL: String = ""
    var TU4: String = ""
    var TU5: String = ""
    var TU6: String = ""
    var TU7: String = ""
    var TU8: String = ""
    
    // Wednesday slots
    var W1: String = ""
    var W2: String = ""
    var W3: String = ""
    var WL: String = ""
    var W4: String = ""
    var W5: String = ""
    var W6: String = ""
    var W7: String = ""
    var W8: String = ""
    
    // Thursday slots
    var TH1: String = ""
    var TH2: String = ""
    var TH3: String = ""
    var THL: String = ""
    var TH4: String = ""
    var TH5: String = ""
    var TH6: String = ""
    var TH7: String = ""
    var TH8: String = ""
    
    // Friday slots
    var F1: String = ""
    var F2: String = ""
    var F3: String = ""
    var FL: String = ""
    var F4: String = ""
    var F5: String = ""
    var F6: String = ""
    var F7: String = ""
    var F8: String = ""
    
    // Saturday slots
    var SA1: String = ""
    var SA2: String = ""
    var SA3: String = ""
    var SAL: String = ""
    var SA4: String = ""
    var SA5: String = ""
    var SA6: String = ""
    var SA7: String = ""
    var SA8: String = ""
}

// Task model for assignments
class Task : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var title: String = ""
    var details: String = ""
    var isDone: Boolean = false
    var dueDate: Long = 0
    var courseTitle: String = ""
}

// Class to initialize and provide Realm instance
object RealmManager {
    private var realm: Realm? = null
    
    fun getRealmInstance(): Realm {
        if (realm == null) {
            val config = RealmConfiguration.create(
                schema = setOf(
                    CellModel::class,
                    CourseTitleModel::class,
                    InstructorModel::class,
                    ScheduleModel::class,
                    CoursenoModel::class,
                    RoomModel::class,
                    ModeModel::class,
                    ColorModel::class,
                    Task::class
                )
            )
            realm = Realm.open(config)
        }
        return realm!!
    }
    
    fun closeRealm() {
        realm?.close()
        realm = null
    }
} 