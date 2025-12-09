package com.example.lifecycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int SHEEP_COUNT = 4;
    private static final int TOTAL_SLOTS = 12;
    private static final int EXTRA_ATTEMPTS = 6;

    private static final String KEY_SLOT_IDENTITIES = "slot_identities";
    private static final String KEY_SLOT_REVEALED = "slot_revealed";
    private static final String KEY_REMAINING_SHEEP = "remaining_sheep";
    private static final String KEY_REMAINING_ATTEMPTS = "remaining_attempts";
    private static final String KEY_STATUS_MESSAGE = "status_message";
    private static final String KEY_SLOT_COUNT = "slot_count";

    private GridLayout characterGrid;
    private TextView sheepCountText;
    private TextView attemptText;
    private TextView statusText;
    private Button resetButton;

    private final List<CharacterSlot> slots = new ArrayList<>();
    private final List<Button> slotButtons = new ArrayList<>();
    private final Random random = new Random();

    private int remainingSheep = SHEEP_COUNT;
    private int remainingAttempts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        characterGrid = findViewById(R.id.characterGrid);
        sheepCountText = findViewById(R.id.sheepCountText);
        attemptText = findViewById(R.id.attemptText);
        statusText = findViewById(R.id.statusText);
        resetButton = findViewById(R.id.resetButton);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupGame();
            }
        });

        if (savedInstanceState != null) {
            restoreGame(savedInstanceState);
        } else {
            setupGame();
        }
    }

    private void setupGame() {
        resetBoardState();

        remainingSheep = SHEEP_COUNT;
        remainingAttempts = SHEEP_COUNT + EXTRA_ATTEMPTS;

        List<Boolean> identities = new ArrayList<>();
        for (int i = 0; i < SHEEP_COUNT; i++) {
            identities.add(true);
        }
        while (identities.size() < TOTAL_SLOTS) {
            identities.add(false);
        }
        Collections.shuffle(identities, random);

        buildSlotsFromIdentities(identities);

        updateStatus("숨은 양을 찾아보세요.");
        updateCounters();
    }

    private void buildSlotsFromIdentities(List<Boolean> identities) {
        for (boolean isSheep : identities) {
            CharacterSlot slot = new CharacterSlot(isSheep);
            slots.add(slot);
            Button slotButton = createSlotButton(slot);
            slotButtons.add(slotButton);
            characterGrid.addView(slotButton);
        }
    }

    private Button createSlotButton(final CharacterSlot slot) {
        Button slotButton = new Button(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = 0;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setGravity(Gravity.FILL);
        slotButton.setLayoutParams(params);
        updateButtonAppearance(slot, slotButton);
        slotButton.setAllCaps(false);

        slotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealSlot(slot, (Button) v);
            }
        });

        return slotButton;
    }

    private void revealSlot(CharacterSlot slot, Button button) {
        if (slot.revealed) {
            Toast.makeText(this, "이미 확인한 대상입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (remainingAttempts <= 0 || remainingSheep <= 0) {
            Toast.makeText(this, "라운드가 종료되었습니다. 새 게임을 시작하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        slot.revealed = true;
        remainingAttempts--;

        if (slot.isSheep) {
            remainingSheep--;
            updateStatus("양을 잡았습니다! 남은 양: " + remainingSheep);
        } else {
            updateStatus("컴퓨터였습니다. 사냥감을 더 찾으세요.");
        }

        updateButtonAppearance(slot, button);
        updateCounters();
        checkRoundEnd();
    }

    private void updateButtonAppearance(CharacterSlot slot, Button button) {
        if (slot.revealed) {
            button.setText(slot.isSheep ? "🐑 양" : "🖥️ 컴퓨터");
            button.setEnabled(false);
        } else {
            button.setText("?");
            button.setEnabled(remainingAttempts > 0 && remainingSheep > 0);
        }
    }

    private void checkRoundEnd() {
        if (remainingSheep == 0) {
            updateStatus("모든 양을 찾아냈습니다! 늑대의 승리입니다.");
            revealAllSlots();
        } else if (remainingAttempts == 0) {
            updateStatus("조사 기회가 모두 소진되었습니다. 양이 도망쳤습니다.");
            revealAllSlots();
        }
    }

    private void revealAllSlots() {
        for (int i = 0; i < slots.size(); i++) {
            CharacterSlot slot = slots.get(i);
            Button button = slotButtons.get(i);

            if (!slot.revealed) {
                slot.revealed = true;
            }

            updateButtonAppearance(slot, button);
        }
    }

    private void updateCounters() {
        sheepCountText.setText("남은 양: " + remainingSheep);
        attemptText.setText("남은 조사: " + remainingAttempts);
    }

    private void updateStatus(String message) {
        statusText.setText(message);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean[] identities = new boolean[slots.size()];
        boolean[] revealed = new boolean[slots.size()];

        for (int i = 0; i < slots.size(); i++) {
            CharacterSlot slot = slots.get(i);
            identities[i] = slot.isSheep;
            revealed[i] = slot.revealed;
        }

        outState.putBooleanArray(KEY_SLOT_IDENTITIES, identities);
        outState.putBooleanArray(KEY_SLOT_REVEALED, revealed);
        outState.putInt(KEY_REMAINING_SHEEP, remainingSheep);
        outState.putInt(KEY_REMAINING_ATTEMPTS, remainingAttempts);
        outState.putString(KEY_STATUS_MESSAGE, statusText.getText().toString());
        outState.putInt(KEY_SLOT_COUNT, slots.size());
    }

    private void restoreGame(Bundle savedInstanceState) {
        boolean[] identities = savedInstanceState.getBooleanArray(KEY_SLOT_IDENTITIES);
        boolean[] revealed = savedInstanceState.getBooleanArray(KEY_SLOT_REVEALED);
        int savedSlotCount = savedInstanceState.getInt(KEY_SLOT_COUNT, TOTAL_SLOTS);

        if (identities == null || revealed == null || identities.length != revealed.length || identities.length != savedSlotCount) {
            setupGame();
            return;
        }

        remainingSheep = savedInstanceState.getInt(KEY_REMAINING_SHEEP, SHEEP_COUNT);
        remainingAttempts = savedInstanceState.getInt(KEY_REMAINING_ATTEMPTS, SHEEP_COUNT + EXTRA_ATTEMPTS);

        resetBoardState();

        for (int i = 0; i < savedSlotCount; i++) {
            CharacterSlot slot = new CharacterSlot(identities[i]);
            slot.revealed = revealed[i];
            slots.add(slot);
            Button button = createSlotButton(slot);
            slotButtons.add(button);
            characterGrid.addView(button);
        }

        updateStatus(savedInstanceState.getString(KEY_STATUS_MESSAGE, "숨은 양을 찾아보세요."));
        updateCounters();

        if (remainingSheep == 0 || remainingAttempts == 0) {
            revealAllSlots();
        }
    }

    private void resetBoardState() {
        slots.clear();
        slotButtons.clear();
        characterGrid.removeAllViews();
    }

    private static class CharacterSlot {
        private final boolean isSheep;
        private boolean revealed;

        CharacterSlot(boolean isSheep) {
            this.isSheep = isSheep;
            this.revealed = false;
        }
    }
}
