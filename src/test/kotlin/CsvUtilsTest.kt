import com.github.renuevo.csv.CsvUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import vo.CsvTestVo

class CsvUtilsTest {

    @Test
    fun csvUtilsModelReadTest() {

        val list = listOf(1, 2, 3)
        val csvUtils = CsvUtils()
        val readList = csvUtils.readModelCsv(javaClass.getResource("csvTest.csv").file, "UTF-8", CsvTestVo::class)

        readList.forEachIndexed{ index, csvTestVo ->
            assertEquals(list[index],csvTestVo.value)
        }
    }

}