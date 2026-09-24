package com.chat.aj.expensetracker.common.Utility;

import com.chat.aj.expensetracker.common.Entities.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMembersRepository groupMembersRepository;
    private final ExpensesRepository expensesRepository;
    private final ExpenseParticipantsRepository expenseParticipantsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        String pw = passwordEncoder.encode("password123");
        LocalDateTime now = LocalDateTime.now();

        // --- Users ---
        User alice   = user("Alice Johnson",  "alice@example.com",  pw, now);
        User bob     = user("Bob Smith",      "bob@example.com",    pw, now);
        User carol   = user("Carol White",    "carol@example.com",  pw, now);
        User david   = user("David Brown",    "david@example.com",  pw, now);
        User emma    = user("Emma Davis",     "emma@example.com",   pw, now);
        User frank   = user("Frank Miller",   "frank@example.com",  pw, now);
        User grace   = user("Grace Wilson",   "grace@example.com",  pw, now);
        User henry   = user("Henry Moore",    "henry@example.com",  pw, now);
        User iris    = user("Iris Taylor",    "iris@example.com",   pw, now);
        User jack    = user("Jack Anderson",  "jack@example.com",   pw, now);

        userRepository.saveAll(List.of(alice, bob, carol, david, emma, frank, grace, henry, iris, jack));

        // --- Group 1: Road Trip (Alice owns; Bob, Carol, David are members) ---
        Group roadTrip = group("Road Trip 2024", alice, now);
        groupRepository.save(roadTrip);
        addMembers(roadTrip, bob, carol, david);

        expense(roadTrip, alice, "Gas fill-up",        "68.40", now.minusDays(10), bob, carol, david);
        expense(roadTrip, bob,   "Hotel night 1",      "240.00", now.minusDays(9), alice, carol, david);
        expense(roadTrip, carol, "Groceries",          "54.80", now.minusDays(9),  alice, bob);
        expense(roadTrip, alice, "Hotel night 2",      "240.00", now.minusDays(8), bob, carol, david);
        expense(roadTrip, david, "Dinner at steakhouse","112.00", now.minusDays(8),alice, bob, carol);
        expense(roadTrip, bob,   "Breakfast",          "38.50", now.minusDays(7),  alice, david);
        expense(roadTrip, alice, "Gas fill-up",        "71.20", now.minusDays(7),  bob, carol, david);
        expense(roadTrip, carol, "Parking fees",       "24.00", now.minusDays(6),  alice, bob, david);

        // --- Group 2: Apartment (Bob owns; Alice, Emma, Frank, Grace are members) ---
        Group apartment = group("Apartment", bob, now);
        groupRepository.save(apartment);
        addMembers(apartment, alice, emma, frank, grace);

        expense(apartment, bob,   "Rent",                 "2000.00", now.minusDays(30), alice, emma, frank, grace);
        expense(apartment, alice, "Electricity bill",     "145.60", now.minusDays(28),  bob, emma, frank);
        expense(apartment, emma,  "Internet",             "60.00", now.minusDays(27),   alice, bob, frank, grace);
        expense(apartment, frank, "Water bill",           "42.00", now.minusDays(26),   alice, bob, emma);
        expense(apartment, grace, "Cleaning supplies",    "31.50", now.minusDays(25),   alice, bob);
        expense(apartment, bob,   "Kitchen restocking",  "88.90", now.minusDays(20),   alice, emma, frank, grace);
        expense(apartment, alice, "Toilet paper & soap", "22.40", now.minusDays(18),   bob, emma);
        expense(apartment, emma,  "Trash bags",          "14.00", now.minusDays(15),   frank, grace);
        expense(apartment, frank, "Light bulbs",         "19.99", now.minusDays(12),   alice, bob, emma, grace);
        expense(apartment, grace, "Dish soap & sponges", "11.50", now.minusDays(10),   alice, bob);
        expense(apartment, bob,   "Rent",                "2000.00", now.minusDays(1),   alice, emma, frank, grace);
        expense(apartment, alice, "Electricity bill",    "138.20", now.minusDays(1),   bob, frank, grace);

        // --- Group 3: Office Lunches (Carol owns; David, Henry are members) ---
        Group officeLunch = group("Office Lunches", carol, now);
        groupRepository.save(officeLunch);
        addMembers(officeLunch, david, henry);

        expense(officeLunch, carol, "Monday lunch",   "42.60", now.minusDays(14), david, henry);
        expense(officeLunch, david, "Tuesday lunch",  "38.90", now.minusDays(13), carol, henry);
        expense(officeLunch, henry, "Wednesday lunch","45.00", now.minusDays(12), carol, david);
        expense(officeLunch, carol, "Thursday lunch", "36.75", now.minusDays(11), david);
        expense(officeLunch, david, "Friday lunch",   "51.20", now.minusDays(10), carol, henry);
        expense(officeLunch, henry, "Monday lunch",   "40.00", now.minusDays(7),  carol, david);

        // --- Group 4: Weekend Getaway (Emma owns; Frank, Grace, Henry, Iris, Jack are members) ---
        Group getaway = group("Weekend Getaway", emma, now);
        groupRepository.save(getaway);
        addMembers(getaway, frank, grace, henry, iris, jack);

        expense(getaway, emma,  "Airbnb booking",      "540.00", now.minusDays(21), frank, grace, henry, iris, jack);
        expense(getaway, frank, "Groceries day 1",     "92.40", now.minusDays(20),  emma, grace, henry);
        expense(getaway, grace, "Dinner restaurant",   "178.50", now.minusDays(20), emma, frank, henry, iris, jack);
        expense(getaway, henry, "Boat rental",         "300.00", now.minusDays(19), emma, frank, grace, iris, jack);
        expense(getaway, iris,  "Sunscreen & supplies","48.00", now.minusDays(19),  emma, frank);
        expense(getaway, jack,  "Breakfast day 2",     "65.30", now.minusDays(19),  grace, henry, iris);
        expense(getaway, emma,  "Groceries day 2",     "87.60", now.minusDays(18),  frank, grace, henry, iris, jack);
        expense(getaway, frank, "Lunch picnic",        "54.20", now.minusDays(18),  emma, grace);
        expense(getaway, grace, "Dinner BBQ",          "134.80", now.minusDays(18), emma, frank, henry, iris, jack);
        expense(getaway, henry, "Ice cream run",       "28.50", now.minusDays(18),  emma, frank, grace);
        expense(getaway, iris,  "Gas",                 "76.40", now.minusDays(17),  emma, frank, grace, henry, jack);
        expense(getaway, jack,  "Toll fees",           "18.00", now.minusDays(17),  emma, iris);
        expense(getaway, emma,  "Car wash",            "22.00", now.minusDays(17),  frank, grace);
        expense(getaway, frank, "Snacks for drive",    "31.70", now.minusDays(17),  emma, grace, henry, iris, jack);
        expense(getaway, grace, "Parking",             "16.00", now.minusDays(17),  emma, frank);

        // --- Group 5: Dinner Club (Iris owns; Alice, Bob, Jack are members) ---
        Group dinnerClub = group("Dinner Club", iris, now);
        groupRepository.save(dinnerClub);
        addMembers(dinnerClub, alice, bob, jack);

        expense(dinnerClub, iris,  "Italian restaurant", "148.00", now.minusDays(28), alice, bob, jack);
        expense(dinnerClub, alice, "Wine & cocktails",   "62.50", now.minusDays(28),  iris, bob);
        expense(dinnerClub, bob,   "Sushi night",        "192.40", now.minusDays(21), alice, iris, jack);
        expense(dinnerClub, jack,  "Dessert",            "34.00", now.minusDays(21),  alice, bob);
        expense(dinnerClub, iris,  "Thai restaurant",    "122.80", now.minusDays(14), alice, bob, jack);
        expense(dinnerClub, alice, "Drinks after dinner","55.00", now.minusDays(14),  iris, jack);
        expense(dinnerClub, bob,   "Mexican night",      "165.30", now.minusDays(7),  alice, iris, jack);
        expense(dinnerClub, jack,  "Tip & extras",       "28.00", now.minusDays(7),   alice, bob);
        expense(dinnerClub, iris,  "French bistro",      "210.60", now.minusDays(1),  alice, bob, jack);
    }

    private User user(String name, String email, String password, LocalDateTime now) {
        User u = new User();
        u.setName(name);
        u.setEmail(email);
        u.setPassword(password);
        u.setCreatedAt(now);
        u.setVerified(true);
        return u;
    }

    private Group group(String name, User owner, LocalDateTime now) {
        Group g = new Group();
        g.setName(name);
        g.setOwner(owner);
        g.setCreatedAt(now);
        return g;
    }

    private void addMembers(Group group, User... users) {
        for (User u : users) {
            GroupMembers gm = new GroupMembers();
            gm.setGroup(group);
            gm.setMember(u);
            groupMembersRepository.save(gm);
        }
    }

    private void expense(Group group, User payer, String description, String amount,
                         LocalDateTime createdAt, User... participants) {
        Expenses e = new Expenses();
        e.setGroup(group);
        e.setUser(payer);
        e.setDescription(description);
        e.setAmount(new BigDecimal(amount));
        e.setCreatedAt(createdAt);
        expensesRepository.save(e);

        BigDecimal total = new BigDecimal(amount);
        int totalPeople = participants.length + 1;
        BigDecimal share = total.divide(BigDecimal.valueOf(totalPeople), 2, java.math.RoundingMode.FLOOR);
        BigDecimal remainder = total.subtract(share.multiply(BigDecimal.valueOf(totalPeople)));

        ExpenseParticipants payerEp = new ExpenseParticipants();
        payerEp.setExpenses(e);
        payerEp.setUser(payer);
        payerEp.setAmount(share.add(remainder));
        expenseParticipantsRepository.save(payerEp);

        for (User participant : participants) {
            ExpenseParticipants ep = new ExpenseParticipants();
            ep.setExpenses(e);
            ep.setUser(participant);
            ep.setAmount(share);
            expenseParticipantsRepository.save(ep);
        }
    }
}
