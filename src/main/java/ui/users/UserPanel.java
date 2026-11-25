package ui.users;

import dao.UserDAO;
import models.User;
import services.AuthService;
import ui.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.List;

public class UserPanel extends JPanel {
    private final UserDAO userDAO;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public UserPanel() {
        this.userDAO = new UserDAO();
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_PANEL);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshTable();
    }

    private void initUI() {
        // Top Action Bar
        JPanel topBar = new JPanel(new BorderLayout(14, 0));
        topBar.setOpaque(false);

        JLabel title = new JLabel("MODULE 1: CAMPUS USER MANAGEMENT");
        title.setFont(Theme.FONT_HEADER_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel actionControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionControls.setOpaque(false);

        searchField = Theme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(240, 34));
        searchField.putClientProperty("JTextField.placeholderText", "Search name, email, dept, role...");

        JButton searchBtn = Theme.createStyledButton("🔍 Search", false);
        searchBtn.addActionListener(e -> performSearch());

        JButton addBtn = Theme.createStyledButton("➕ Add User", true);
        addBtn.addActionListener(e -> openUserDialog(null));

        JButton editBtn = Theme.createStyledButton("✏ Edit Selected", false);
        editBtn.addActionListener(e -> editSelectedUser());

        JButton delBtn = Theme.createDangerButton("🗑 Delete");
        delBtn.addActionListener(e -> deleteSelectedUser());

        JButton refreshBtn = Theme.createStyledButton("🔄 Refresh", false);
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            refreshTable();
        });

        actionControls.add(searchField);
        actionControls.add(searchBtn);
        actionControls.add(addBtn);
        actionControls.add(editBtn);
        actionControls.add(delBtn);
        actionControls.add(refreshBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionControls, BorderLayout.EAST);

        // Table Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout());

        String[] cols = {"User ID", "Username", "Full Name", "Email Address", "Department", "Role", "Status", "Phone Number", "Created Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        userTable = new JTable(tableModel);
        Theme.styleTable(userTable);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1));
        card.add(scrollPane, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(card, BorderLayout.CENTER);
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<User> list = userDAO.getAllUsers();
        for (User u : list) {
            tableModel.addRow(new Object[] {
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getDepartment(),
                u.getRole(),
                u.getStatus(),
                (u.getPhoneNumber() != null) ? u.getPhoneNumber() : "N/A",
                u.getCreatedAt()
            });
        }
    }

    private void performSearch() {
        String q = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<User> list = userDAO.searchUsers(q);
        for (User u : list) {
            tableModel.addRow(new Object[] {
                u.getUserId(),
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                u.getDepartment(),
                u.getRole(),
                u.getStatus(),
                (u.getPhoneNumber() != null) ? u.getPhoneNumber() : "N/A",
                u.getCreatedAt()
            });
        }
    }

    private void editSelectedUser() {
        int selected = userTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) tableModel.getValueAt(selected, 0);
        User u = userDAO.getUserById(userId);
        if (u != null) {
            openUserDialog(u);
        }
    }

    private void deleteSelectedUser() {
        int selected = userTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (int) tableModel.getValueAt(selected, 0);
        String username = (String) tableModel.getValueAt(selected, 1);
        String role = (String) tableModel.getValueAt(selected, 5);

        // Safeguard 1: Do not delete current user
        if (AuthService.getInstance().getCurrentUser() != null &&
            AuthService.getInstance().getCurrentUser().getUserId() == userId) {
            JOptionPane.showMessageDialog(this, "Safeguard: You cannot delete the currently authenticated account.", "Action Blocked", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Safeguard 2: Confirmation
        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format("Are you sure you want to delete user '%s' [%s] (ID: %d)?\nAll associated device and activity records may be cascade-affected.", username, role, userId),
            "Confirm User Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = userDAO.deleteUser(userId);
            if (ok) {
                JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user. Check database constraints.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openUserDialog(User existing) {
        boolean isEdit = (existing != null);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), isEdit ? "Edit Campus User" : "Add New Campus User", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setOpaque(false);

        JTextField uField = Theme.createStyledTextField();
        JTextField pField = Theme.createStyledTextField();
        JTextField nameField = Theme.createStyledTextField();
        JTextField emailField = Theme.createStyledTextField();
        JTextField deptField = Theme.createStyledTextField();
        JComboBox<String> roleCb = Theme.createStyledComboBox(new String[]{"STUDENT", "FACULTY", "STAFF", "INVESTIGATOR", "ADMIN"});
        JComboBox<String> statCb = Theme.createStyledComboBox(new String[]{"ACTIVE", "SUSPENDED", "INACTIVE"});
        JTextField phoneField = Theme.createStyledTextField();

        if (isEdit) {
            uField.setText(existing.getUsername());
            uField.setEnabled(false); // Username fixed on edit
            pField.setText("[Unchanged]");
            pField.setEnabled(false);
            nameField.setText(existing.getFullName());
            emailField.setText(existing.getEmail());
            deptField.setText(existing.getDepartment());
            roleCb.setSelectedItem(existing.getRole());
            statCb.setSelectedItem(existing.getStatus());
            phoneField.setText((existing.getPhoneNumber() != null) ? existing.getPhoneNumber() : "");
        } else {
            pField.setText("password123");
        }

        form.add(createLabel("Username:"));
        form.add(uField);
        form.add(createLabel("Password:"));
        form.add(pField);
        form.add(createLabel("Full Name:"));
        form.add(nameField);
        form.add(createLabel("Campus Email:"));
        form.add(emailField);
        form.add(createLabel("Department:"));
        form.add(deptField);
        form.add(createLabel("Role:"));
        form.add(roleCb);
        form.add(createLabel("Status:"));
        form.add(statCb);
        form.add(createLabel("Phone Number:"));
        form.add(phoneField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton saveBtn = Theme.createStyledButton(isEdit ? "Update User" : "Save User", true);
        JButton cancelBtn = Theme.createStyledButton("Cancel", false);
        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            String uname = uField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String dept = deptField.getText().trim();
            String role = (String) roleCb.getSelectedItem();
            String status = (String) statCb.getSelectedItem();
            String phone = phoneField.getText().trim();

            if (uname.isEmpty() || name.isEmpty() || email.isEmpty() || dept.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all mandatory fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit) {
                existing.setFullName(name);
                existing.setEmail(email);
                existing.setDepartment(dept);
                existing.setRole(role);
                existing.setStatus(status);
                existing.setPhoneNumber(phone);
                boolean ok = userDAO.updateUser(existing);
                if (ok) {
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                User nu = new User();
                nu.setUsername(uname);
                nu.setPasswordHash(AuthService.hashPassword(pField.getText().trim()));
                nu.setFullName(name);
                nu.setEmail(email);
                nu.setDepartment(dept);
                nu.setRole(role);
                nu.setStatus(status);
                nu.setPhoneNumber(phone);
                nu.setCreatedAt(new Timestamp(System.currentTimeMillis()));

                boolean ok = userDAO.addUser(nu);
                if (ok) {
                    dialog.dispose();
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add user. Username or email may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnBar.add(saveBtn);
        btnBar.add(cancelBtn);

        panel.add(form, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_BOLD);
        l.setForeground(Theme.TEXT_SECONDARY);
        return l;
    }
}
