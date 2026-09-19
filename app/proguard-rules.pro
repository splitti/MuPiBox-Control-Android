# Gson uses reflection for API DTOs and DataStore persistence models.
-keepattributes Signature,*Annotation*
-keep class de.mupibox.control.data.api.** { *; }
-keep class de.mupibox.control.model.** { *; }
