-- Add department_hierarchy column to complaints table to store ML-assigned escalation chain

ALTER TABLE complaints
ADD COLUMN IF NOT EXISTS department_hierarchy TEXT;

COMMENT ON COLUMN complaints.department_hierarchy IS 'ML-assigned department escalation hierarchy chain (e.g., "AE > JE > EE > Commissioner")';
