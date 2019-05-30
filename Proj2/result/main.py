import pymysql.cursors

# A set of queries that drop all tables in database and reset table schemas
# These queries was automatically made from MySQLWorkbench EER
RESET_SQL = ["DROP TABLE IF EXISTS Assignment;",
             "DROP TABLE IF EXISTS Book;",
             "DROP TABLE IF EXISTS Building;",
             "DROP TABLE IF EXISTS Performance;",
             "DROP TABLE IF EXISTS Audience;",
             '''
                CREATE TABLE IF NOT EXISTS `Building` (
                `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
                `Name` VARCHAR(200) CHARACTER SET 'utf8' NULL,
                `Location` VARCHAR(200) CHARACTER SET 'utf8' NULL,
                `Capacity` INT UNSIGNED NULL,
                PRIMARY KEY (`ID`))
                ENGINE = InnoDB;
             ''',
             '''
                CREATE TABLE IF NOT EXISTS `Performance` (
                  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
                  `Name` VARCHAR(200) CHARACTER SET 'utf8' NULL,
                  `Type` VARCHAR(200) CHARACTER SET 'utf8' NULL,
                  `Price` INT UNSIGNED NULL,
                  PRIMARY KEY (`ID`))
                ENGINE = InnoDB;
             ''',
             '''
                CREATE TABLE IF NOT EXISTS `Audience` (
                  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
                  `Name` VARCHAR(200) CHARACTER SET 'utf8' NULL,
                  `Gender` VARCHAR(1) CHARACTER SET 'utf8' NULL,
                  `Age` INT UNSIGNED NULL,
                  PRIMARY KEY (`ID`))
                ENGINE = InnoDB;
             ''',
             '''
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
                    ON DELETE CASCADE
                    ON UPDATE NO ACTION)
                ENGINE = InnoDB;
             ''',
             '''
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
             ''']

# helper function
# construct list which entries contain maximum string length of i-th column
# this maximum value will be used to calculate space bound
def get_max_size(rows, l):
    if len(rows) == 0:
        return [0 for _ in range(l)]
    maxsize = [0 for _ in range(len(rows[0]))]
    for row in rows:
        for i in range(len(row)):
            maxsize[i] = max(maxsize[i], len(str(row[i])))
    return maxsize

# helper function
# Print all records and title
def print_records(title, rows):
    length = len(title)
    maxsize = get_max_size(rows, length)
    basesize = [len(attr) for attr in title]
    for i in range(length):
        maxsize[i] = max(maxsize[i], basesize[i])
    line = [0] * length
    for i in range(length):
        line[i] = "{" + str(i) + ":" + str(maxsize[i]) + "}"
    line = "    ".join(line)
    print("--------------------------------------------------------------------------------")
    print(line.format(*title))
    print("--------------------------------------------------------------------------------")
    for row in rows:
        print(line.format(*map(str, row)))
    print("--------------------------------------------------------------------------------\n")
    return

# start function
# print all options that application can perform
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


# Option 1
# Print out all buildings in database
# Additionally, addpend the number of performances which assigned to each building to output
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

    title = ["id", "name", "location", "capacity", "assigned"]
    print_records(title, rows)

# Option 2
# Print out all performances in database
# Additionally, addpend the number of distinct audience who booked each performance to output
def print_performances(cursor):
    sql = '''
            SELECT ID, Name, Type, Price, Count(A_ID)
            FROM (SELECT DISTINCT ID, Name, Type, Price, A_ID FROM Performance LEFT JOIN Book ON(ID = P_ID)) as T
            GROUP BY ID
            ORDER BY ID;
        '''
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()

    title = ["id", "name", "type", "price", "booked"]
    print_records(title, rows)
    return


# Option 3
# Print out all audiences in database
def print_audiences(cursor):
    sql = '''
            SELECT ID, Name, Gender, Age
            FROM Audience
            ORDER BY ID;
    '''
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()

    title = ["id", "name", "gender", "age"]
    print_records(title, rows)
    return


# Option 4
# Insert a building into Building table
def add_building(cursor):
    # Input
    name = input("Building name: ")
    location = input("Building location: ")
    capacity = int(input("Building capacity: "))

    # capacity check
    if capacity < 1:
        print("Capacity should be more than 0\n")
        return

    # Insert
    sql = '''INSERT INTO Building (Name, Location, Capacity)
             VALUES("{0}", "{1}", {2})'''.format(name[:200], location[:200], capacity)
    cursor.execute(sql)
    connection.commit()
    print("A building is successfully inserted\n")


# Option 5
# Delete a building from Building table
def remove_building(cursor):
    # Input
    b_id = int(input("Building ID: "))

    # Building existence check
    sql = "SELECT ID FROM Building WHERE ID = {0}".format(b_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Building {0} doesn’t exist\n".format(b_id))
        return

    # Delete
    sql = "DELETE FROM Building WHERE ID = {0}".format(b_id)
    cursor.execute(sql)
    connection.commit()
    print("A building is successfully removed\n")


# Option 6
# Insert a performance into Performance table
def add_performance(cursor):
    # Input
    name = input("Performance name: ")
    type = input("Performance type: ")
    price = int(input("Performance price: "))

    # Price range check
    if price < 0:
        print("Price should be 0 or more\n")
        return

    # Insert
    sql = '''INSERT INTO Performance (Name, Type, Price)
             VALUES("{0}", "{1}", {2})'''.format(name[:200], type[:200], price)
    cursor.execute(sql)
    connection.commit()
    print("A performance is successfully inserted\n")


# Option 7
# Delete a performance from Performance table
def remove_performance(cursor):
    # Input
    p_id = int(input("performance ID: "))

    # Performance existence check
    sql = "SELECT ID FROM Performance WHERE ID = {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Performance {0} doesn’t exist\n".format(p_id))
        return

    # Delete
    sql = '''DELETE FROM Performance WHERE ID = {0}'''.format(p_id)
    cursor.execute(sql)
    connection.commit()
    print("A performance is successfully removed\n")


# Option 8
# Insert a audience into Audience table
def add_audience(cursor):
    # Input
    name = input("Audience name: ")
    gender = input("Audience gender: ")[:1]

    # Gender check
    if gender != 'F' and gender != 'M':
        print("Gender should be 'M' or 'F'\n")
        return

    # Input age
    age = int(input("Audience age: "))

    # Age range check
    if age < 1:
        print("Age should be more than 0\n")
        return

    # Insert
    sql = '''INSERT INTO Audience (Name, Gender, Age) VALUES("{0}", "{1}", {2})'''.format(name[:200], gender, age)
    cursor.execute(sql)
    connection.commit()
    print("An audience is successfully inserted\n")


# Option 9
# Delete a audience from Audience table
def remove_audience(cursor):
    # Input
    a_id = int(input("Audience ID: "))

    # Audience existence check
    sql = "SELECT ID FROM Audience WHERE ID = {0}".format(a_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Audience {0} doesn’t exist\n".format(a_id))
        return

    # Delete
    sql = '''DELETE FROM Audience WHERE ID = {0}'''.format(a_id)
    cursor.execute(sql)
    connection.commit()
    print("An audience is successfully removed\n")


# Option 10
# Matching building and performance
def performance_assign(cursor):
    # Input building id
    b_id = int(input("Building ID: "))

    sql = "SELECT ID FROM Building WHERE ID = {0}".format(b_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Building {0} doesn’t exist\n".format(b_id))
        return

    # Input performance id
    p_id = int(input("Performance ID: "))

    # Beforehand, should check whether the performance is already assigned
    sql = "SELECT P_ID FROM Assignment WHERE P_ID = {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is not None:
        print("Performance {0} is already assigned\n".format(p_id))
        return

    # Assign
    sql = '''
            INSERT INTO Assignment
            (P_ID, B_ID) VALUES({0}, {1})'''.format(p_id, b_id)
    cursor.execute(sql)
    connection.commit()
    print("Successfully assign a performance\n")
    return


# Option 11
# Book a performance in the name of a audience
def performance_book(cursor):
    # Input performance id which the audience want to book
    p_id = int(input("Performance ID: "))

    # Beforehand, should check whether the performance is assigned
    # Get building id and building capacity
    sql = "SELECT ID, Capacity FROM Assignment, Building WHERE P_ID = {0} and ID = B_ID".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Performance {0} isn't assigned\n".format(p_id))
        return

    b_id = row[0]
    capacity = row[1]

    # Get price of the performance
    # Do not need checking whether performance is exist because previous query verified it.
    sql = "SELECT Price FROM Performance WHERE ID = {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    price = row[0]

    # Input audience id who want to book the performance
    a_id = int(input("Audience ID: "))

    # Get age of the audience
    sql = "SELECT Age FROM Audience WHERE ID = {0}".format(a_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Audience {0} doesn’t exist\n".format(a_id))
        return
    age = row[0]

    # Input seat list which the audience want
    seat_number_list = [int(x) for x in input("Seat Number: ").strip().split(',')]

    # Get already booked seats "booked_seat"
    sql = "SELECT Seat FROM Book WHERE P_ID = {0}".format(p_id)
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()
    booked_seat = []
    if rows is not None:
        booked_seat = [row[0] for row in rows]

    # If one of the desirous seats is already booked, rollback previous queries and return error message
    for seat_number in seat_number_list:
        if capacity < seat_number or seat_number < 1:
            print("Seat number out of range\n")
            connection.rollback()
            return
        if seat_number in booked_seat:
            print("The seat is already taken\n")
            connection.rollback()
            return
        sql = "INSERT INTO Book (A_ID, P_ID, B_ID, Seat) VALUES({0}, {1}, {2}, {3})".format(a_id, p_id, b_id, seat_number)
        cursor.execute(sql)
    connection.commit()

    # Apply discount ratio to price according to discount policy
    discount = 1
    if age < 8:
        discount = 0
    elif age < 13:
        discount = 0.5
    elif age < 19:
        discount = 0.8

    print("Successfully book a performance Total ticket price is {0}\n".format(int(round(len(seat_number_list) * price * discount, 0))))


# Option 12
# Print all performances assigned to a building
# Additionally, show the number of booked seats
def print_assignment(cursor):
    # Input
    b_id = int(input("Building ID: "))

    # Building existence check
    sql = "SELECT ID FROM Building WHERE ID = {0}".format(b_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Building {0} doesn’t exist\n".format(b_id))
        return

    # Get all performances which are assigned to the building
    # And the number of booked seats of each performance
    sql = '''
            SELECT ID, Name, Type, Price, Count(Seat)
            FROM Assignment JOIN Performance ON(ID = P_ID) LEFT JOIN Book ON(ID = Book.P_ID)
            WHERE Assignment.B_ID = {0}
            GROUP BY ID'''.format(b_id)
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()

    title = ["id", "name", "type", "price", "booked"]
    print_records(title, rows)
    return

# Option 13
# Given performance id, Print all audiences who have booked the performance
def print_audience_book(cursor):
    # Input
    p_id = int(input("Performance ID: "))

    # Performance existence check
    sql = "SELECT ID FROM Performance WHERE ID =  {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Performance {0} doesn’t exist\n".format(p_id))
        return

    # Get audiences who have booked the performance
    sql = '''
            SELECT DISTINCT ID, Name, Gender, Age
            FROM Book JOIN Audience ON(ID = A_ID)
            WHERE P_ID = {0}
            '''.format(p_id)
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()

    title = ["id", "name", "gender", "age"]
    print_records(title, rows)
    return


# Option 14
# Given performance id, Print all seats and whether each seat is booked
def print_seat_booked(cursor):
    # Input
    p_id = int(input("Performance ID: "))

    # Performance existence check & assigned check
    sql = "SELECT ID, B_ID FROM Performance LEFT JOIN Assignment ON(ID = P_ID) WHERE ID =  {0}".format(p_id)
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    if row is None:
        print("Performance {0} doesn’t exist\n".format(p_id))
        return

    if row[1] is None:
        print("Performance {0} isn't assigned\n".format(p_id))
        return

    # Get capacity of the building where the performance assigned to
    sql = "SELECT Capacity FROM Building WHERE ID =  {0}".format(row[1])
    cursor.execute(sql)
    row = cursor.fetchone()
    connection.commit()
    capacity = row[0]

    # Get all audiences who have booked the performance and seat numbers according to each audience
    sql = '''
            SELECT Seat, A_ID
            FROM Book
            WHERE P_ID = {0}
            '''.format(p_id)
    cursor.execute(sql)
    rows = cursor.fetchall()
    connection.commit()
    audiences_list = ["" for _ in range(capacity)]
    for row in rows:
        audiences_list[row[0]-1] = str(row[1])

    title = ["seat_number", "audience_id"]
    print_records(title, list(enumerate(audiences_list, start=1)))
    return


# Option 16
# Reset database
# execute all queries in RESET_SQL consecutively
def reset_db(cursor):
    do = input("Do you really want to reset this database?(y/n) ");
    if do == "y":
        for sql in RESET_SQL:
            cursor.execute(sql)
        connection.commit()
    return


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
        print("Invalid action\n")
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
                print("Bye!")
                break
            elif(action == 16):
                reset_db(cur)
            else:
                print("Invalid action\n")
    except():
        break
connection.close()
