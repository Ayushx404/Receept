# Agent Rules: Think Architecturally

When asked to implement ANY feature:

1. **Study the existing codebase first.** Look at how similar features
   are already implemented. Follow the same patterns.
2. **Trace the data flow.** Before writing code, identify:

   * Where does this data come from? (API, database, local storage, state)
   * Where does it need to go? (all layers, not just the UI)
   * What other parts of the app depend on this data?

3. **Apply changes at the SOURCE, not the symptom.**

   * If data comes from a database, modify the database
   * Don't just patch the UI or local state

4. **Consider side effects:**

   * Related/dependent data that needs updating
   * Cache invalidation
   * Other screens that display the same data
   * Undo/rollback scenarios

5. **When in doubt, ASK.** Don't assume the simplest interpretation.
   State your assumptions before writing code.
6. **Test your mental model:** After implementing, ask yourself:
   "If the user restarts the app / refreshes the page, does this
   change persist correctly?"
