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
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
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

def print_buildings(cursor):
    sql = '''
            SELECT ID, Name, Location, Capacity, COUNT(P_ID) as C
            FROM Building LEFT JOIN Assignment ON(ID = B_ID)  
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


def print_performances(cursor):
    sql = '''
            SELECT ID, Name, Type, Price, COUNT(Seat) as C
            FROM Performance LEFT JOIN Book ON(ID = P_ID)  
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


def print_audiences(cursor):
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


def add_building(cursor):
    name = input("Building name: ")
    location = input("Building location: ")
    capacity = int(input("Building capacity: "))

    sql = '''
            INSERT INTO Building
            (Name, Location, Capacity)
            VALUES("{0}", "{1}", {2})
    '''.format(name, location, capacity)
    cursor.execute(sql)
    connection.commit()
    print("A building is successfully inserted\n")


def remove_building(cursor):
    identifier = int(input("Building ID: "))

    sql = '''
            DELETE FROM Building
            WHERE ID = {0}'''.format(identifier)
    cursor.execute(sql)
    connection.commit()
    print("the building is successfully deleted\n")


def add_performance(cursor):
    name = input("Performance name: ")
    type = input("Performance type: ")
    price = int(input("Performance price: "))

    sql = '''
            INSERT INTO Performance
            (Name, Type, Price)
            VALUES("{0}", "{1}", {2})
    '''.format(name, type, price)
    cursor.execute(sql)
    connection.commit()
    print("A performance is successfully inserted\n")


def remove_performance(cursor):
    identifier = int(input("performance ID: "))

    sql = '''
            DELETE FROM Performance
            WHERE ID = {0}'''.format(identifier)
    cursor.execute(sql)
    connection.commit()
    print("the performance is successfully deleted\n")


def add_audience(cursor):
    name = input("Performance name: ")
    gender = input("Performance gender: ")
    age = int(input("Performance age: "))

    sql = '''
            INSERT INTO Audience
            (Name, Gender, Age)
            VALUES("{0}", "{1}", {2})
    '''.format(name, gender, age)
    cursor.execute(sql)
    connection.commit()
    print("A audience is successfully inserted\n")


def remove_audience(cursor):
    identifier = int(input("Audience ID: "))

    sql = '''
            DELETE FROM Audience
            WHERE ID = {0}'''.format(identifier)
    cursor.execute(sql)
    connection.commit()
    print("the audience is successfully deleted\n")

def reset_db(cursor):
    for sql in RESET_SQL:
        cursor.execute(sql)
    connection.commit()
    return


def performance_assign(cursor):
    b_id = int(input("Building ID: "))
    p_id = int(input("Performance ID: "))
    sql = "SELECT P_ID FROM Assignment WHERE P_ID = {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    print(row)
    if row is not None:
        print("This performance is already assigned to other building")
        return
    sql = '''
            INSERT INTO Assignment
            (P_ID, B_ID) VALUES({0}, {1})'''.format(p_id, b_id)
    cursor.execute(sql)
    connection.commit()
    print("the performance is successfully assigned to the building\n")
    return


def performance_book(cursor):
    p_id = int(input("Performance ID: "))

    sql = "SELECT Price FROM Performance WHERE ID = {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("the performance is not in Database")
        return
    price = row[0]

    sql = "SELECT ID, Capacity FROM Assignment, Building WHERE P_ID = {0} and ID = B_ID".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("the performance is not assigned")
        return

    b_id = row[0]
    capacity = row[1]

    a_id = int(input("Audience ID: "))

    sql = "SELECT Age FROM Audience WHERE ID = {0}".format(a_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("the audience is not in Database")
        return
    age = row[0]

    seat_number_list = [int(x) for x in input("Seat Number: ").strip().split(',')]

    sql = "SELECT Seat FROM Book WHERE P_ID = {0}".format(p_id)
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()
    booked_seat = []
    if rows is not None:
        booked_seat = [row[0] for row in rows]
    for seat_number in seat_number_list:
        if capacity < seat_number or seat_number < 1:
            print("invalid seat number")
            connection.rollback()
            return
        if seat_number in booked_seat:
            print("some seat is already booked")
            connection.rollback()
            return
        sql = "INSERT INTO Book (A_ID, P_ID, B_ID, Seat) VALUES({0}, {1}, {2}, {3})".format(a_id, p_id, b_id, seat_number)
        cursor.execute(sql)
    connection.commit()

    discount = 1
    if age < 8:
        discount = 0
    elif age < 13:
        discount = 0.5
    elif age < 19:
        discount = 0.8

    print("price: " + str(round(len(seat_number_list) * price * discount, 0)))


def print_assignment(cursor):
    b_id = int(input("Building ID: "))
    sql = '''
            SELECT Name, Type, Price, Count(Seat)
            FROM Assignment JOIN Performance ON(ID = P_ID) LEFT JOIN Book ON(ID = Book.P_ID) 
            WHERE Assignment.B_ID = {0}
            GROUP BY ID'''.format(b_id)
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()

    maxsize = [len(str(b_id))] + get_max_size(rows, 4)
    basesize = [2, 4, 4, 5, 6]
    for i in range(len(maxsize)):
        maxsize[i] = max(maxsize[i], basesize[i])
    FORMAT = ["{0:" + str(maxsize[0]) + "}", "{1:" + str(maxsize[1]) + "}", "{2:" + str(maxsize[2]) + "}",
              "{3:" + str(maxsize[3]) + "}", "{4:" + str(maxsize[4]) + "}"]
    FORMAT = "    ".join(FORMAT)
    print("--------------------------------------------------------------------------------")
    print(FORMAT.format("id", "name", "type", "price", "booked"))
    print("--------------------------------------------------------------------------------")
    for row in rows:
        [name, type, price, booked] = row
        print(FORMAT.format(str(b_id), name, type, str(price), str(booked)))
    print("--------------------------------------------------------------------------------\n")


def print_audience_book(cursor):
    p_id = int(input("Performance ID: "))
    sql = "SELECT ID FROM Performance WHERE ID =  {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("the performance is not in Database")
        return

    sql = '''
            SELECT DISTINCT ID, Name, Gender, Age
            FROM Book JOIN Audience ON(ID = A_ID) 
            '''
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()

    maxsize = get_max_size(rows, 4)
    basesize = [2, 4, 5, 3]
    for i in range(len(maxsize)):
        maxsize[i] = max(maxsize[i], basesize[i])
    FORMAT = ["{0:" + str(maxsize[0]) + "}", "{1:" + str(maxsize[1]) + "}", "{2:" + str(maxsize[2]) + "}",
              "{3:" + str(maxsize[3]) + "}"]
    FORMAT = "    ".join(FORMAT)
    print("--------------------------------------------------------------------------------")
    print(FORMAT.format("id", "name", "gender", "age"))
    print("--------------------------------------------------------------------------------")
    for row in rows:
        [identifier, name, gender, age] = row
        print(FORMAT.format(str(identifier), name, gender, str(age)))
    print("--------------------------------------------------------------------------------\n")


def print_seat_booked(cursor):
    p_id = int(input("Performance ID: "))
    sql = "SELECT ID FROM Performance WHERE ID =  {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("the performance is not in Database")
        return

    sql = '''
            SELECT A_ID, Seat
            FROM Book
            WHERE P_ID = {0}
            '''.format(p_id)
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()

    maxsize = get_max_size(rows, 4)
    basesize = [2, 4, 5, 3]
    for i in range(len(maxsize)):
        maxsize[i] = max(maxsize[i], basesize[i])
    FORMAT = ["{0:" + str(maxsize[0]) + "}", "{1:" + str(maxsize[1]) + "}", "{2:" + str(maxsize[2]) + "}",
              "{3:" + str(maxsize[3]) + "}"]
    FORMAT = "    ".join(FORMAT)
    print("--------------------------------------------------------------------------------")
    print(FORMAT.format("id", "name", "gender", "age"))
    print("--------------------------------------------------------------------------------")
    for row in rows:
        [identifier, name, gender, age] = row
        print(FORMAT.format(str(identifier), name, gender, str(age)))
    print("--------------------------------------------------------------------------------\n")



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
        with connection.cursor() as cur:
            if(action == 1):
                print_buildings(cur)
            elif(action == 2):
                print_performances(cur)
            elif(action == 3):
                print_audiences(cur)
            elif(action == 4):
                add_building(cur)
            elif(action == 5):
                remove_building(cur)
            elif(action == 6):
                add_performance(cur)
            elif(action == 7):
                remove_performance(cur)
            elif(action == 8):
                add_audience(cur)
            elif(action == 9):
                remove_audience(cur)
            elif(action == 10):
                performance_assign(cur)
            elif(action == 11):
                performance_book(cur)
            elif(action == 12):
                print_assignment(cur)
            elif(action == 13):
                print_audience_book(cur)
            elif(action == 14):
                print_seat_booked(cur)
            elif(action == 15):
                break
            elif(action == 16):
                reset_db(cur)
            else:
                print("Please put the integer between 1 to 16")
    except():
        break
connection.close()
