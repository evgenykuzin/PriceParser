import org.jekajops.parser.exel.DataManagerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import static org.jekajops.app.cnfg.TableConfig.ID_COL_NAME;
import static org.jekajops.app.cnfg.TableConfig.PRICE_COL_NAME;
import static org.jekajops.worker.Worker.getUpdatedOzonTable;

public class Tests {
    @Test
    public void test() throws Throwable {
        var dataManager = DataManagerFactory.getOzonWebCsvManager();
        var maps = dataManager.parseTable();
        var maps2 = getUpdatedOzonTable(maps);
    }

    public String[] trans(Map<String, Map<String, String>> mapMap) {
        var res = mapMap.values().stream().sorted(Comparator.comparingInt(m -> Integer.parseInt(m.get(ID_COL_NAME)))).map(map -> map.get(PRICE_COL_NAME)).toArray(String[]::new);
        System.out.println("res = " + Arrays.toString(res));
        return res;
    }
}
