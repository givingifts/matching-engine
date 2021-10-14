package gifts.givin.matching.common

import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.uchuhimo.konf.source.yaml

fun getConfig() = Config { addSpec(MatcherSpec) }
    .from.yaml.file("matcher.yml")
    .from.env()
    .from.systemProperties()

object MatcherSpec : ConfigSpec() {
    val db_url by optional("jdbc:mysql://localhost:3306/matching?useSSL=false")
    val db_user by optional("matching")
    val db_pass by optional("matching")
    val max_instances by optional(10)
    val instances_percentage by optional(50)
    val use_script by optional(false)
}
