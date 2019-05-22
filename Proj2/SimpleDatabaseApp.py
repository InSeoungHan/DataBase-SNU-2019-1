import pymysql.cursors
general_string_size = 200 # this exist for convenience. you should know all schemas in the Database.

RESET_SQL = ["DROP TABLE IF EXISTS Assignment;", "DROP TABLE IF EXISTS Book;", "DROP TABLE IF EXISTS Building;", "DROP TABLE IF EXISTS Performance;", "DROP TABLE IF EXISTS Audience;"
    , "SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;"
    , "SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;"
    , "SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';"
    , '''
    CREATE TABLE IF NOT EXISTS `Building` (
    `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `Name` VARCHAR(200) CHARACTER SET 'utf8' NULL,
    `Location` VARCHAR(200) CHARACTER SET 'utf8' NULL,
    `Capacity` INT UNSIGNED NULL,
    PRIMARY KEY (`ID`))
    ENGINE = InnoDB;
    '''
,'''
CREATE TABLE IF NOT EXISTS `Performance` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(200) CHARACTER SET 'utf8' NULL,
  `Type` VARCHAR(200) CHARACTER SET 'utf8' NULL,
  `Price` INT UNSIGNED NULL,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB;
'''
,'''
CREATE TABLE IF NOT EXISTS `Audience` (
  `ID` INT UNSIGNED NOT NULL,
  `Name` VARCHAR(200) CHARACTER SET 'utf8' NULL,
  `Gender` VARCHAR(1) CHARACTER SET 'utf8' NULL,
  `Age` INT UNSIGNED NULL,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB;
'''

,'''
CREATE TABLE IF NOT EXISTS `Assignment` (
  `P_ID` INT UNSIGNED NOT NULL,
  `B_ID` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`P_ID`, `B_ID`),
  INDEX `Building-idx` (`B_ID` ASC),
  CONSTRAINT `Assigment-B`
    FOREIGN KEY (`B_ID`)
    REFERENCES `Building` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `Assignment-P`
    FOREIGN KEY (`P_ID`)
    REFERENCES `Performance` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
'''
,'''
CREATE TABLE IF NOT EXISTS `Book` (
  `A_ID` INT UNSIGNED NULL,
  `P_ID` INT UNSIGNED NOT NULL,
  `B_ID` INT UNSIGNED NOT NULL,
  `Seat` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`Seat`, `B_ID`, `P_ID`),
  INDEX `Audience_idx` (`A_ID` ASC),
  INDEX `Building_idx` (`B_ID` ASC),
  INDEX `Performance_idx` (`P_ID` ASC),
  CONSTRAINT `Book-P`
    FOREIGN KEY (`P_ID`)
    REFERENCES `Performance` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `Book-A`
    FOREIGN KEY (`A_ID`)
    REFERENCES `Audience` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `Book-B`
    FOREIGN KEY (`B_ID`)
    REFERENCES `Building` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
'''

,"SET SQL_MODE=@OLD_SQL_MODE;"
,"SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;"
,"SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;"]


def get_max_size(rows, l):
    if len(rows) == 0:
        return [0 for _ in range(l)]
    maxsize = [0 for _ in range(len(rows[0]))]
    for row in rows:
        for i in range(len(row)):
            maxsize[i] = max(maxsize[i], len(str(row[i])))
    return maxsize

def start():
    print('''============================================================
    1. print all buildings
    2. print all performances
    3. print all audiences
    4. insert a new building
    5. remove a building
    6. insert a new performance
    7. remove a performance
    8. insert a new audience
    9. remove an audience
    10. assign a performance to a building
    11. book a performance
    12. print all performances which assigned at a building
    13. print all audiences who booked for a performance
    14. print ticket booking status of a performance
    15. exit
    16. reset database
    ============================================================'''
          )

def print_buildings():
    with connection.cursor() as cursor:
        sql = '''
                SELECT ID, Name, Location, Capacity, COUNT(P_ID) as C
                FROM Building LEFT JOIN Assignment ON ID = B_ID  
                GROUP BY ID
                ORDER BY ID;
        '''
        cursor.execute(sql)
        rows = cursor.fetchall()
    connection.commit()

    maxsize = get_max_size(rows, 5)
    basesize = [2, 4, 8, 8, 8]
    for i in range(len(maxsize)):
        maxsize[i] = max(maxsize[i], basesize[i])
    FORMAT = ["{0:" + str(maxsize[0]) + "}", "{1:" + str(maxsize[1]) + "}", "{2:" + str(maxsize[2]) + "}", "{3:" + str(maxsize[3]) + "}", "{4:" + str(maxsize[4]) + "}"]
    FORMAT = "    ".join(FORMAT)
    print("--------------------------------------------------------------------------------")
    print(FORMAT.format("id", "name", "location", "capacity", "assigned"))
    print("--------------------------------------------------------------------------------")
    for row in rows:
        [identifier, name, location, capacity, assigned] = row
        print(FORMAT.format(str(identifier), name, location, str(capacity), str(assigned)))
    print("--------------------------------------------------------------------------------\n")
    return


def print_performances():
    with connection.cursor() as cursor:
        sql = '''
                SELECT ID, Name, Type, Price, COUNT(Seat) as C
                FROM Performance LEFT JOIN Book ON ID = P_ID  
                GROUP BY ID
                ORDER BY ID;
        '''
        cursor.execute(sql)
        rows = cursor.fetchall()
    connection.commit()

    maxsize = get_max_size(rows, 5)
    basesize = [2, 4, 4, 5, 6]
    for i in range(len(maxsize)):
        maxsize[i] = max(maxsize[i], basesize[i])
    FORMAT = ["{0:" + str(maxsize[0]) + "}", "{1:" + str(maxsize[1]) + "}", "{2:" + str(maxsize[2]) + "}", "{3:" + str(maxsize[3]) + "}", "{4:" + str(maxsize[4]) + "}"]
    FORMAT = "    ".join(FORMAT)
    print("--------------------------------------------------------------------------------")
    print(FORMAT.format("id", "name", "type", "price", "booked"))
    print("--------------------------------------------------------------------------------")
    for row in rows:
        [identifier, name, type, price, booked] = row
        print(FORMAT.format(str(identifier), name, type, str(price), str(booked)))
    print("--------------------------------------------------------------------------------\n")
    return


def print_audiences():
    with connection.cursor() as cursor:
        sql = '''
                SELECT ID, Name, Gender, Age
                FROM Audience
                ORDER BY ID;
        '''
        cursor.execute(sql)
        rows = cursor.fetchall()
    connection.commit()

    maxsize = get_max_size(rows, 4)
    basesize = [2, 4, 3, 3]
    for i in range(len(maxsize)):
        maxsize[i] = max(maxsize[i], basesize[i])
    FORMAT = ["{0:" + str(maxsize[0]) + "}", "{1:" + str(maxsize[1]) + "}", "{2:" + str(maxsize[2]) + "}", "{3:" + str(maxsize[3]) + "}"]
    FORMAT = "    ".join(FORMAT)
    print("--------------------------------------------------------------------------------")
    print(FORMAT.format("id", "name", "gender", "age"))
    print("--------------------------------------------------------------------------------")
    for row in rows:
        [identifier, name, gender, age] = row
        print(FORMAT.format(str(identifier), name, gender, str(age)))
    print("--------------------------------------------------------------------------------\n")
    return


def add_building():
    name = str(input("Building name: "))
    location = str(input("Building location: "))
    capacity = int(input("Building capacity: "))

    with connection.cursor() as cursor:
        sql = '''
                INSERT INTO Building
                (Name, Location, Capacity)
                VALUES("{0}", "{1}", {2})
        '''.format(name, location, capacity)
        cursor.execute(sql)
    connection.commit()
    print("A building is successfully inserted\n")


def remove_building():
    identifier = int(input("Building ID: "))

    with connection.cursor() as cursor:
        sql = '''
                DELETE FROM Building
                WHERE ID = {0}'''.format(identifier)
        cursor.execute(sql)
    connection.commit()
    print("the building is successfully deleted\n")


def add_performance():
    name = str(input("Performance name: "))
    type = str(input("Performance type: "))
    price = int(input("Performance price: "))

    with connection.cursor() as cursor:
        sql = '''
                INSERT INTO Performance
                (Name, Type, Price)
                VALUES("{0}", "{1}", {2})
        '''.format(name, type, price)
        cursor.execute(sql)
    connection.commit()
    print("A performance is successfully inserted\n")


def remove_performance():
    identifier = int(input("performance ID: "))

    with connection.cursor() as cursor:
        sql = '''
                DELETE FROM Performance
                WHERE ID = {0}'''.format(identifier)
        cursor.execute(sql)
    connection.commit()
    print("the performance is successfully deleted\n")


def add_audience():
    name = str(input("Performance name: "))
    gender = str(input("Performance gender: "))
    age = int(input("Performance age: "))

    with connection.cursor() as cursor:
        sql = '''
                INSERT INTO Audience
                (Name, Gender, Age)
                VALUES("{0}", "{1}", {2})
        '''.format(name, gender, age)
        cursor.execute(sql)
    connection.commit()
    print("A audience is successfully inserted\n")


def remove_audience():
    identifier = int(input("Audience ID: "))

    with connection.cursor() as cursor:
        sql = '''
                DELETE FROM Audience
                WHERE ID = {0}'''.format(identifier)
        cursor.execute(sql)
    connection.commit()
    print("the audience is successfully deleted\n")

def reset_db():
    with connection.cursor() as cursor:
        for sql in RESET_SQL:
            cursor.execute(sql)
    connection.commit()
    return


def performance_book():
    b_id = int(input("Building ID: "))
    p_id = int(input("Performance ID: "))

    with connection.cursor() as cursor:
        sql = '''
                INSERT INTO Assignment
                (P_ID, B_ID) VALUES({0}, {1}'''.format(p_id, b_id)
        cursor.execute(sql)
    connection.commit()
    print("the performance is successfully assigned to the building\n")
    return


def performance_assign():
    pass


def print_assignment():
    pass


def print_audience_book():
    pass


def print_performance_book():
    pass



# Connect to the database
connection = pymysql.connect(host='s.snu.ac.kr',
                             port=3306,
                             user='DB2014_10469',
                             password='DB2014_10469',
                             database='DB2014_10469'
                             # charset='utf8mb4',
                             # cursorclass=pymysql.cursors.DictCursor
                             )

start()
while True:
    try:
        action = int(input("Select your action: "))
    except(ValueError):
        print("Please put the integer between 1 to 16")
        continue
    try:
        if(action == 1):
            print_buildings()
        elif(action == 2):
            print_performances()
        elif(action == 3):
            print_audiences()
            print("d")
        elif(action == 4):
            add_building()
        elif(action == 5):
            remove_building()
        elif(action == 6):
            add_performance()
        elif(action == 7):
            remove_performance()
        elif(action == 8):
            add_audience()
        elif(action == 9):
            remove_audience()
        elif(action == 10):
            performance_assign()
        elif(action == 11):
            performance_book()
        elif(action == 12):
            print_assignment()
        elif(action == 13):
            print_audience_book()
        elif(action == 14):
            print_performance_book()
        elif(action == 15):
            break
        elif(action == 16):
            reset_db()
        else:
            print("Please put the integer between 1 to 16")
    except():
        break
connection.close()
