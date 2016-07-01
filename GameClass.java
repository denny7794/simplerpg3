package simplerpg;

import java.util.Random;
import java.util.Scanner;

public class GameClass {

    private Hero[] heroPattern = new Hero[3];
    private Monster[] monsterPattern = new Monster[3];
    private Hero mainHero;
    private Monster currentMonster;
    private GameMap map;
    private InGameShop shop;
    private int currentRound;

    private int inpInt;

    public GameClass() {
        initGame();
    }   

    public void mainGameLoop() // Метод, отвечающий за игровую логику
    {
        map = new GameMap();
        shop = new InGameShop();        
        inpInt = 0;
        System.out.println("Игра началась!");
        selectHero();
        mainHero.setXY(10, 3);
        map.buildDangMap(10, 3);
        map.updateMap(mainHero.getX(), mainHero.getY());
        map.showMap();
        while (true) {
            int x = getAction(1, 6, "Что Вы хотите делать дальше 1. Пойти влево; 2. Пойти вправо; 3. Пойти вверх; 4. Пойти вниз; 5. Найти монстров; 6. Отдохнуть");            
            
            switch (x) {
                case 1:
                    if(map.isCellEmpty(mainHero.getX() - 1, mainHero.getY()))
                        mainHero.moveHero(-1, 0);
                    break;
                case 2:
                    if(map.isCellEmpty(mainHero.getX() + 1, mainHero.getY()))
                        mainHero.moveHero(1, 0);
                    break;
                case 3:
                    if(map.isCellEmpty(mainHero.getX(), mainHero.getY() - 1))
                        mainHero.moveHero(0, -1);
                    break;
                case 4:
                    if(map.isCellEmpty(mainHero.getX(), mainHero.getY() + 1))
                        mainHero.moveHero(0, 1);
                    break;
                case 5:
                    currentMonster = (Monster) monsterPattern[1].clone();  // Создаем монстра путем копирования из шаблона                               
                    currentMonster.lvlUp(map.getDangerous(mainHero.getX(), mainHero.getY()));
                    battle(mainHero, currentMonster);                    
                case 6:
                    mainHero.fullHeal();
                    break;
            }

            if(map.getObstValue(mainHero.getX(), mainHero.getY()) == 'S')
                shopAction();
            
            if(Utils.rand.nextInt(100) < 3)
            {
                System.out.println("На Вас внезапно напали!!!");
                currentMonster = (Monster) monsterPattern[1].clone();  // Создаем монстра путем копирования из шаблона                               
                currentMonster.lvlUp(map.getDangerous(mainHero.getX(), mainHero.getY()));
                battle(mainHero, currentMonster); 
            }
            
            map.updateMap(mainHero.getX(), mainHero.getY());
            map.showMap();
            
            if (!mainHero.isAlive()) {
                break;
            }
        }

        System.out.println("Игра завершена");
    }

    public void selectHero() {
        String s = "Выберите героя: ";
        for (int i = 0; i < 3; i++) {
            s += (i + 1) + ". " + heroPattern[i].getName() + "   ";
        }
        inpInt = getAction(1, 3, s);
        mainHero = (Hero) heroPattern[inpInt - 1].clone(); // Создаем героя путем копирования из шаблона
        System.out.println(mainHero.getName() + " начал свое путешествие");
    }

    public void battle(Hero h, Monster m) {
        currentRound = 1;
        System.out.println("Бой между игроком " + h.getName() + " и монстром " + m.getName() + " начался");
        do {
            System.out.println("Текущий раунд: " + currentRound);
            h.showInfo();
            m.showInfo();
            h.makeNewRound(); // Вызываем метод сброса параметров героя на начало раунда            
            inpInt = getAction(0, 3, "Ход игрока: 1. Атака 2. Защита 3. Покопаться в сумке 0. Завершить игру");
            System.out.print("\n\n"); // Печатаем два символа перевода строки
            if (inpInt == 1) // Герой атакует
            {
                m.getDamage(h.makeAttack()); // Вызываем метод получения урона монстром
                if (!m.isAlive()) // Делаем проверку жив ли монстр после удара героя
                {
                    System.out.println(m.getName() + " погиб"); // Печатаем сообщение о гибели монстра
                    h.expGain(m.getHpMax() * 2); // Даем герою опыта в размере (Здоровье_монстра * 2)
                    h.addKillCounter();
                    m.myInv.transferAllItemsToAnotherInventory(h.myInv);
                    System.out.println("На поле боя выходит " + m.getName()); // Выводим сообщение о выходе нового врага на поле боя
                    break;
                }
            }
            if (inpInt == 2) // Герой защищается
            {
                h.setBlockStance(); // Вызывем метод включения защитной стойки
            }
            if (inpInt == 3) {
                h.myInv.showAllItems();
                int invInput = getAction(0, h.myInv.getSize(), "Выберите предмет для использования");
                String usedItem = h.myInv.useItem(invInput);
                if (usedItem != "") {
                    System.out.println(h.getName() + " использовал " + usedItem);
                    h.useItem(usedItem);
                } else {
                    System.out.println(h.getName() + " просто закрыл сумку");
                }
            }
            if (inpInt == 0) {
                break; // Выход из игры
            }
            m.makeNewRound();  // Вызываем метод сброса параметров монстра на начало раунда

            if (Utils.rand.nextInt(100) < 80) {
                h.getDamage(m.makeAttack()); // По умолчанию, монстр всегда атакует
            } else {
                m.setBlockStance();
            }

            if (!h.isAlive()) // Если после удара монстра герой погибает - выходим из основного игрового цикла
            {
                break;
            }

            currentRound++;
        } while (true);
        if (m.isAlive() && h.isAlive()) {
            System.out.println(h.getName() + " сбежал с поля боя");
        }
        if (!m.isAlive()) {
            System.out.println("Победил " + h.getName());
        }
        if (!h.isAlive()) {
            System.out.println("Победил " + m.getName());
        }
    }
    
    public void shopAction()
    {        
        shop.showItems();
        System.out.println("Для выхода из магазина нажмите 0");
        int x = getAction(0, shop.ITEMS_COUNT, "Введите номер покупаемого товара");
        if (x == 0) return;
        shop.buyByHero(x - 1, mainHero);
    }

    public void initGame() // Инициализируем начальное состояние игры
    {
        // Задаем шаблоны героев и монстров
        heroPattern[0] = new Hero("Knight", "Lancelot", 16, 8, 12);
        heroPattern[1] = new Hero("Barbarian", "Konan", 16, 8, 12);
        heroPattern[2] = new Hero("Dwarf", "Gimli", 16, 8, 12);
        monsterPattern[0] = new Monster("Humanoid", "Goblin", 12, 4, 4);
        monsterPattern[1] = new Monster("Humanoid", "Orc", 18, 6, 6);
        monsterPattern[2] = new Monster("Humanoid", "Troll", 32, 12, 10);
        currentRound = 1;
    }

    public int getAction(int _min, int _max, String _str) {
        int x = 0;
        do {
            if (_str != "") {
                System.out.println(_str);
            }
            x = Utils.sc.nextInt();
        } while (x < _min || x > _max);
        return x;
    }
}
