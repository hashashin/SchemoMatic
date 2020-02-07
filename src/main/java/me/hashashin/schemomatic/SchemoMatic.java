package me.hashashin.schemomatic;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.cloutteam.samjakob.gui.ItemBuilder;
import com.cloutteam.samjakob.gui.buttons.GUIButton;
import com.cloutteam.samjakob.gui.config.PaginatedInventoryConfiguration;
import com.cloutteam.samjakob.gui.types.PaginatedGUI;

import java.io.File;
import java.util.*;

public final class SchemoMatic extends JavaPlugin {

    private Map<String, String> schems = new HashMap<>();
    private Map<String, String> currentFolder = new HashMap<>();
    private PaginatedGUI foldersGUI;
    private PaginatedInventoryConfiguration guiConfig = new PaginatedInventoryConfiguration();

    @Override
    public void onEnable() {
        // Register the listeners for the PaginatedGUI API
        PaginatedGUI.prepare(this);
        getCommand("schemgui").setExecutor(this);
        GetFiles();
        guiConfig.setChatPrefix(ChatColor.DARK_BLUE + this.getName() + ": " + ChatColor.RESET);
    }

    @Override
    public void onDisable() {
        this.getPluginLoader().disablePlugin(this);
    }

    private void GetFiles() {
        this.schems.clear();
        File _parentFolder = new File("plugins/WorldEdit/schematics");
        if (!_parentFolder.exists())
            _parentFolder.mkdirs();
        byte b;
        int i;
        String[] _array;
        for (i = (_array = _parentFolder.list()).length, b = 0; b < i; ) {
            String _folders = _array[b];
            File _folder = new File("plugins/WorldEdit/schematics/" + _folders);
            if (_folder.list() == null) {
                this.schems.put(_folders, "root");
            } else {
                byte b1;
                int j;
                String[] _array1;
                for (j = (_array1 = _folder.list()).length, b1 = 0; b1 < j ; ) {
                    String _file = _array1[b1];
                    this.schems.put(_file, _folders);
                    b1++;
                }
            }
            b++;
        }
    }

    private void openBrowser() {
        Map<String, Integer> _foldersIndex = new HashMap<>();
        List<String> _folders = new ArrayList<>();
        for (String _file : this.schems.keySet()) {
            String _folder = this.schems.get(_file);
            if (!_folders.contains(_folder))
                _folders.add(_folder);
            if (!_foldersIndex.containsKey(_folder))
                _foldersIndex.put(_folder, 0);
            _foldersIndex.put(_folder, _foldersIndex.get(_folder) + 1);
        }
        String[] _sortedFolders = _folders.toArray(new String[_folders.size()]);
        Arrays.sort(_sortedFolders);
        byte b;
        int j;
        String[] _array;
        this.foldersGUI = new PaginatedGUI("Schematics Folders", guiConfig);

        for (j = (_array = _sortedFolders).length, b = 0; b < j; ) {
            String _folder = _array[b];
            GUIButton _button = new GUIButton(
                    ItemBuilder.start(Material.PLAYER_HEAD).name(_folder, "mattijs").build()
            );
            _button.setListener(event -> {
                event.setCancelled(true);
                openBrowser((Player)event.getWhoClicked(), event.getCurrentItem().getItemMeta().getDisplayName());
                this.currentFolder.put(event.getWhoClicked().getName(), event.getCurrentItem().getItemMeta()
                        .getDisplayName());
            });
            this.foldersGUI.addButton(_button);
            b++;
        }
    }

    private void openBrowser(Player p, String folder) {
        Set<String> _index = this.schems.keySet();
        List<String> _files = new ArrayList<>();
        for (String _file : _index) {
            if (this.schems.get(_file).equalsIgnoreCase(folder))
                _files.add(_file);
        }
        String[] _sortedFiles = _files.toArray(new String[0]);
        Arrays.sort(_sortedFiles);
        byte b;
        int j;
        String[] _array;
        PaginatedGUI _filesGUI = new PaginatedGUI("Schematics Browser", guiConfig);
        for (j = (_array = _sortedFiles).length, b = 0; b < j; ) {
            String file = _array[b];
            GUIButton _button = new GUIButton(
                    ItemBuilder.start(Material.PLAYER_HEAD).name(file.replaceAll("\\..*",""),
                            "Afinity")
                            .lore(Arrays.asList(file.replaceAll(".*\\.", ""))).build()
            );
            _button.setListener(event -> {
                event.setCancelled(true);
                onInventoryClick(event);
            });
            _filesGUI.addButton(_button);
            b++;
        }
        GUIButton _return = new GUIButton(
                ItemBuilder.start(Material.PLAYER_HEAD).name("&a&lReturn", "MHF_ArrowLeft")
                        .lore("&fGo to folders.").build()
        );
        _return.setListener(event -> {
            event.setCancelled(true);
            p.getPlayer().openInventory(foldersGUI.getInventory());
        });
        if (_filesGUI.getFinalPage() != 0) {
            _filesGUI.setToolbarItem(8, _return);
        }
        else {
            _filesGUI.setButton(b+1,_return);
        }
        p.getPlayer().openInventory(_filesGUI.getInventory());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String alias, String[] args) {
        GetFiles();
        openBrowser();
        ((Player) sender).getPlayer().openInventory(foldersGUI.getInventory());
        return true;
    }

    private void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        } else {
            e.getView().getTitle();
        }
        e.getWhoClicked().closeInventory();
        if (this.currentFolder.get(e.getWhoClicked().getName()).equals("root")) {
            Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "/schem load "
                    + e.getCurrentItem().getItemMeta().getDisplayName() + "."
                    + e.getCurrentItem().getItemMeta().getLore().get(0));
        } else {
            Bukkit.getServer().dispatchCommand(e.getWhoClicked(), "/schem load "
                    + this.currentFolder.get(e.getWhoClicked().getName()) + "/"
                    + e.getCurrentItem().getItemMeta().getDisplayName() + "."
                    + e.getCurrentItem().getItemMeta().getLore().get(0));
        }
    }
}


