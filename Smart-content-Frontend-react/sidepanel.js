document.addEventListener("DOMContentLoaded", () => {
  chrome.storage.local.get(["researchNotes"], function (result) {
    if (result.researchNotes) {
      document.getElementById("notes").value = result.researchNotes;
    }
  });

  const summarizeBtn = document.getElementById("summerizeBtn");
  const saveNotesBtn = document.getElementById("saveNotesBtn");

  if (summarizeBtn) {
    summarizeBtn.addEventListener("click", summarizeText);
  } else {
    console.error("Error: Element with ID 'summerizeBtn' not found.");
  }

  if (saveNotesBtn) {
    saveNotesBtn.addEventListener("click", saveNotes);
  } else {
    console.error("Error: Element with ID 'saveNotesBtn' not found.");
  }
});

async function summarizeText() {
  try {
    const [tab] = await chrome.tabs.query({
      active: true,
      currentWindow: true,
    });
    if (!tab) {
      shownResult("Error: No active tab found.");
      return;
    }

    const [{ result }] = await chrome.scripting.executeScript({
      target: { tabId: tab.id },
      function: () => window.getSelection().toString(),
    });

    if (!result) {
      shownResult("Please select some text first.");
      return;
    }

    const response = await fetch("http://localhost:8080/api/Content/process", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content: result, operation: "suggest" }),
    });

    if (!response.ok) {
      throw new Error(`API Error: ${response.status}`);
    }

    const text = await response.text();
    shownResult(text.replace(/\n/g, "<br>"));
  } catch (error) {
    shownResult("Error: " + error.message);
  }
}

async function saveNotes() {
  const notes = document.getElementById("notes").value;
  chrome.storage.local.set({ researchNotes: notes }, function () {
    alert("Notes Saved successfully");
  });
}

function shownResult(content) {
  document.getElementById(
    "results"
  ).innerHTML = `<div class="result-item"><div class="result-content">${content}</div></div>`;
}
