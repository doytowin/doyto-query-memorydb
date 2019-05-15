package win.doyto.query.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MenuControllerTest
 *
 * @author f0rb
 * @date 2019-05-15
 */
class MenuControllerTest {

    @Test
    void get() {
        MockMenuMapper menuMapper = new MockMenuMapper();
        MenuController menuController = new MenuController(new MenuService(menuMapper));

        MenuRequest menuRequest = new MenuRequest();
        menuRequest.setParentId(0);
        menuRequest.setMenuName("root");
        menuController.save(menuRequest);

        MenuResponse menuResponse = menuController.get(1);
        assertEquals("root", menuResponse.getMenuName());
        assertEquals(0, (int) menuResponse.getParentId());
    }

}