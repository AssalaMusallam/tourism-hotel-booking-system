import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Edit, Trash2, BarChart2 } from 'lucide-react';
import { format, parseISO, isPast } from 'date-fns';
import toast from 'react-hot-toast';
import {
  usePricingRules,
  useCreatePricingRule,
  useUpdatePricingRule,
  useDeletePricingRule,
  usePricePreview,
} from '../../hooks/usePricingRules';
import PricingRuleForm from '../../components/pricing/PricingRuleForm';
import PriceBreakdownCard from '../../components/pricing/PriceBreakdownCard';
import MultiplierBadge from '../../components/pricing/MultiplierBadge';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Spinner from '../../components/ui/Spinner';
import Pagination from '../../components/ui/Pagination';
import EmptyState from '../../components/ui/EmptyState';
import Input from '../../components/ui/Input';
import SectionError from '../../components/ui/SectionError';
import { parseApiError } from '../../lib/parseApiError';
import styles from './ManagePricingRules.module.css';

// ── Timeline ────────────────────────────────────────────────────────────────
const TIMELINE_COLORS = ['#f59e0b', '#14b8a6', '#8b5cf6', '#f43f5e', '#6366f1'];

const RulesTimeline = ({ rules }) => {
  if (!rules?.length) return null;

  const today = new Date();
  const dates = rules.flatMap((r) => [new Date(r.startDate), new Date(r.endDate)]);
  let minDate = new Date(Math.min(...dates));
  let maxDate = new Date(Math.max(...dates));
  // Extend a month on each side for breathing room
  minDate = new Date(minDate.getFullYear(), minDate.getMonth() - 1, 1);
  maxDate = new Date(maxDate.getFullYear(), maxDate.getMonth() + 2, 0);
  const span = maxDate - minDate || 1;

  const pct = (date) =>
    Math.max(0, Math.min(100, ((new Date(date) - minDate) / span) * 100));

  const todayPct = pct(today);
  const showToday = todayPct >= 0 && todayPct <= 100;

  return (
    <div className={styles.timeline}>
      <div className={styles.timelineTrack}>
        {showToday && (
          <div className={styles.todayLine} style={{ left: `${todayPct}%` }} title="Today" />
        )}
        {rules.map((rule, i) => {
          const left = pct(rule.startDate);
          const width = pct(rule.endDate) - left;
          const color = TIMELINE_COLORS[i % TIMELINE_COLORS.length];
          return (
            <div
              key={rule.id}
              className={styles.ruleBar}
              style={{ left: `${left}%`, width: `${Math.max(width, 0.5)}%`, background: color }}
              title={`${rule.name} · ×${rule.priceMultiplier} · ${rule.startDate} → ${rule.endDate}`}
            >
              {width > 8 && <span className={styles.barLabel}>{rule.name}</span>}
            </div>
          );
        })}
      </div>
      <div className={styles.timelineLabels}>
        <span>{format(minDate, 'MMM yyyy')}</span>
        <span>Today</span>
        <span>{format(maxDate, 'MMM yyyy')}</span>
      </div>
    </div>
  );
};

// ── Status badge ─────────────────────────────────────────────────────────────
const RuleStatusBadge = ({ rule }) => {
  if (isPast(parseISO(rule.endDate))) {
    return <span className={styles.badgeExpired}>Expired</span>;
  }
  return rule.active
    ? <span className={styles.badgeActive}>Active</span>
    : <span className={styles.badgeInactive}>Inactive</span>;
};

// ── Price Calculator sandbox ──────────────────────────────────────────────────
const PriceCalculator = () => {
  const [basePrice, setBasePrice] = useState('');
  const [checkIn, setCheckIn] = useState('');
  const [checkOut, setCheckOut] = useState('');
  const [enabled, setEnabled] = useState(false);

  const { data, isFetching, isError, error, refetch } = usePricePreview(
    Number(basePrice) || 0,
    checkIn,
    checkOut,
    enabled && !!basePrice && !!checkIn && !!checkOut
  );

  const handleCalculate = () => {
    if (!basePrice || !checkIn || !checkOut) {
      toast.error('Fill in all fields before calculating');
      return;
    }
    setEnabled(true);
  };

  return (
    <section className={styles.calculator}>
      <h2 className={styles.calcTitle}>🧮 Price Calculator</h2>
      <p className={styles.calcSub}>Test how rules affect pricing before guests see them.</p>
      <div className={styles.calcRow}>
        <Input
          label="Base price / night ($)"
          type="number"
          min="1"
          step="1"
          value={basePrice}
          onChange={(e) => { setBasePrice(e.target.value); setEnabled(false); }}
          placeholder="e.g. 100"
        />
        <Input
          label="Check-in"
          type="date"
          value={checkIn}
          onChange={(e) => { setCheckIn(e.target.value); setEnabled(false); }}
        />
        <Input
          label="Check-out"
          type="date"
          value={checkOut}
          onChange={(e) => { setCheckOut(e.target.value); setEnabled(false); }}
        />
        <div className={styles.calcBtnWrap}>
          <Button variant="primary" onClick={handleCalculate} loading={isFetching}>
            Calculate Preview
          </Button>
        </div>
      </div>
      {isFetching && <div className="skeleton" style={{ height: 180, width: '100%', marginTop: 16 }} />}
      {!isFetching && isError && (
        <SectionError message={parseApiError(error).message} onRetry={refetch} />
      )}
      {!isFetching && data && <div className={styles.calcResult}><PriceBreakdownCard breakdown={data} /></div>}
    </section>
  );
};

// ── Main page ─────────────────────────────────────────────────────────────────
const ManagePricingRules = () => {
  const [tab, setTab] = useState('all');
  const [page, setPage] = useState(0);
  const [showTimeline, setShowTimeline] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editRule, setEditRule] = useState(null);
  const [overlapError, setOverlapError] = useState(null);

  const activeOnly = tab === 'active';
  const { data, isLoading, isError, error, refetch } = usePricingRules(page, activeOnly);
  const rules = data?.content || [];
  const totalPages = data?.totalPages || 0;

  const createMutation = useCreatePricingRule();
  const updateMutation = useUpdatePricingRule();
  const deleteMutation = useDeletePricingRule();

  const openCreate = () => { setEditRule(null); setOverlapError(null); setModalOpen(true); };
  const openEdit = (rule) => { setEditRule(rule); setOverlapError(null); setModalOpen(true); };
  const closeModal = () => { setModalOpen(false); setEditRule(null); setOverlapError(null); };

  const handleRuleMutationError = (err) => {
    const parsed = parseApiError(err);
    if (parsed.status === 409) {
      setOverlapError(parsed.message);
      toast.error('Date conflict with existing rule.');
      return;
    }
    toast.error(parsed.message);
  };

  const handleSubmit = (formData) => {
    setOverlapError(null);
    if (editRule) {
      updateMutation.mutate({ id: editRule.id, data: formData }, {
        onSuccess: () => {
          toast.success('Pricing rule updated');
          closeModal();
        },
        onError: handleRuleMutationError,
      });
    } else {
      createMutation.mutate(formData, {
        onSuccess: () => {
          toast.success('Pricing rule created');
          closeModal();
        },
        onError: handleRuleMutationError,
      });
    }
  };

  const handleDelete = (rule) => {
    if (window.confirm(`Delete "${rule.name}"? This cannot be undone.`)) {
      deleteMutation.mutate(rule.id, {
        onSuccess: () => toast.success('Pricing rule deleted'),
        onError: (err) => {
          const parsed = parseApiError(err);
          toast.error(parsed.status === 404 ? 'Pricing rule not found.' : parsed.message);
          if (parsed.status === 404) refetch();
        },
      });
    }
  };

  const fmtPeriod = (rule) => {
    const start = format(parseISO(rule.startDate), 'MMM d, yyyy');
    const end = format(parseISO(rule.endDate), 'MMM d, yyyy');
    const days = Math.round((new Date(rule.endDate) - new Date(rule.startDate)) / (1000 * 60 * 60 * 24));
    return `${start} → ${end} (${days} days)`;
  };

  const isMutating = createMutation.isPending || updateMutation.isPending;

  return (
    <div className={styles.page}>
      <aside className={styles.sidebar}>
        <nav className={styles.nav}>
          <Link to="/admin" className={styles.navLink}>Dashboard</Link>
          <Link to="/admin/hotels" className={styles.navLink}>Manage Hotels</Link>
          <Link to="/admin/amenities" className={styles.navLink}>Manage Amenities</Link>
          <Link to="/dashboard/users" className={styles.navLink}>Manage Users</Link>
          <Link to="/dashboard/pricing-rules" className={`${styles.navLink} ${styles.active}`}>Pricing Rules</Link>
        </nav>
      </aside>

      <main className={styles.main}>
        <div className={styles.header}>
          <div>
            <h1>Pricing Rules</h1>
            <p className={styles.headerSub}>Seasonal multipliers applied to base room prices.</p>
          </div>
          <div className={styles.headerActions}>
            <button
              className={`${styles.iconBtn} ${showTimeline ? styles.iconBtnActive : ''}`}
              onClick={() => setShowTimeline((v) => !v)}
              title="Toggle timeline view"
            >
              <BarChart2 size={16} />
              Timeline
            </button>
            <Button variant="primary" icon={Plus} onClick={openCreate}>
              New Pricing Rule
            </Button>
          </div>
        </div>

        {showTimeline && <RulesTimeline rules={data?.content || []} />}

        {/* Tabs */}
        <div className={styles.tabs}>
          <button
            className={`${styles.tab} ${tab === 'all' ? styles.tabActive : ''}`}
            onClick={() => { setTab('all'); setPage(0); }}
          >
            All Rules
          </button>
          <button
            className={`${styles.tab} ${tab === 'active' ? styles.tabActive : ''}`}
            onClick={() => { setTab('active'); setPage(0); }}
          >
            Active Rules
          </button>
        </div>

        {/* Table */}
        {isLoading ? (
          <Spinner centered />
        ) : isError ? (
          <SectionError message={parseApiError(error).message} onRetry={refetch} />
        ) : rules.length === 0 ? (
          <EmptyState
            title="No pricing rules found"
            description="Create a seasonal rule to adjust room prices automatically."
            action={{ label: 'New Pricing Rule', onClick: openCreate }}
          />
        ) : (
          <>
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Period</th>
                    <th>Multiplier</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rules.map((rule) => {
                    const expired = isPast(parseISO(rule.endDate));
                    return (
                      <tr key={rule.id} className={`${styles.row} ${expired ? styles.rowExpired : ''}`}>
                        <td className={styles.nameCell}>
                          <span className={expired ? styles.expired : ''}>{rule.name}</span>
                          {rule.description && (
                            <span className={styles.desc}>{rule.description}</span>
                          )}
                        </td>
                        <td className={styles.periodCell}>{fmtPeriod(rule)}</td>
                        <td><MultiplierBadge multiplier={rule.priceMultiplier} /></td>
                        <td><RuleStatusBadge rule={rule} /></td>
                        <td>
                          <div className={styles.actions}>
                            <button
                              className={styles.actionBtn}
                              onClick={() => openEdit(rule)}
                              title="Edit"
                            >
                              <Edit size={15} />
                            </button>
                            <button
                              className={`${styles.actionBtn} ${styles.deleteBtn}`}
                              onClick={() => handleDelete(rule)}
                              title="Delete"
                            >
                              <Trash2 size={15} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
          </>
        )}

        <PriceCalculator />
      </main>

      <Modal
        isOpen={modalOpen}
        onClose={closeModal}
        title={editRule ? 'Edit Pricing Rule' : 'New Pricing Rule'}
        size="lg"
      >
        <PricingRuleForm
          rule={editRule}
          onSubmit={handleSubmit}
          loading={isMutating}
          overlapError={overlapError}
        />
      </Modal>
    </div>
  );
};

export default ManagePricingRules;
