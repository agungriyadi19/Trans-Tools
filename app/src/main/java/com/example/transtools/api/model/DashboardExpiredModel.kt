import com.example.transtools.api.model.ClosestItem
import com.google.gson.annotations.SerializedName

data class DashboardExpiredModel(
    @SerializedName("total_item_nearing_expiration") var totalItemNearingExpiration: String?,
    @SerializedName("nearest_expiration_date") var nearestExpirationDate: String?,
    @SerializedName("closest_items") var closestItems: List<ClosestItem>?
)